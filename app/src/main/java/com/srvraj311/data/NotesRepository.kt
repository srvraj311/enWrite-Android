package com.srvraj311.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

enum class SyncState { Synced, Saving, Offline, Error }

private const val SAVE_DELAY_MS = 600L

/**
 * All notes for the signed-in user, kept in step with Firestore (users/{email}/note/{id}).
 *
 * Edits show up instantly, are written after a short pause, and are queued by Firestore's offline cache when the
 * device has no connection. Guest notes live in a local file instead.
 */
class NotesRepository(private val context: Context, private val auth: AuthRepository) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val db = FirebaseFirestore.getInstance()
    private val guestFile = File(context.filesDir, "guest_notes.json")

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    private val _loading = MutableStateFlow(true)
    private val _online = MutableStateFlow(true)
    private val serverPending = MutableStateFlow(false)
    private val localPending = MutableStateFlow(0)
    private val failed = MutableStateFlow(false)

    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    val online: StateFlow<Boolean> = _online.asStateFlow()

    /** True while any edit has not been confirmed by the server. */
    val unsynced: StateFlow<Boolean> = combine(localPending, serverPending) { l, s -> l > 0 || s }
        .stateIn(scope, SharingStarted.Eagerly, false)

    val sync: StateFlow<SyncState> = combine(failed, _online, unsynced) { f, on, un ->
        when {
            f -> SyncState.Error
            !on -> SyncState.Offline
            un -> SyncState.Saving
            else -> SyncState.Synced
        }
    }.stateIn(scope, SharingStarted.Eagerly, SyncState.Synced)

    private var registration: ListenerRegistration? = null
    private val saveJobs = mutableMapOf<String, Job>()
    /** Notes created on this device that have not been written yet (kept even if a snapshot does not contain them). */
    private val drafts = mutableSetOf<String>()
    private var email: String? = null
    private var guest = false

    init {
        watchNetwork()
        scope.launch { auth.user.collect { restart(it) } }
    }

    // ---- backends -------------------------------------------------------------------------

    private fun restart(user: AppUser?) {
        registration?.remove()
        registration = null
        saveJobs.values.forEach { it.cancel() }
        saveJobs.clear()
        drafts.clear()
        localPending.value = 0
        serverPending.value = false
        failed.value = false
        email = null
        guest = false
        when {
            user == null -> { _notes.value = emptyList(); _loading.value = false }
            user.guest -> { guest = true; _notes.value = readGuest(); _loading.value = false }
            else -> startFirestore(user.email)
        }
    }

    private fun collection(email: String) = db.collection("users").document(email).collection("note")

    private fun startFirestore(email: String) {
        this.email = email
        _loading.value = true
        registration = collection(email).addSnapshotListener(MetadataChanges.INCLUDE) { snap, error ->
            if (error != null || snap == null) { _loading.value = false; failed.value = true; return@addSnapshotListener }
            val incoming = LinkedHashMap<String, Note>()
            for (d in snap.documents) incoming[d.id] = Note.fromMap(d.id, d.data.orEmpty())
            // Keep edits that have not been written yet, and brand-new notes that only exist here.
            val local = _notes.value.associateBy { it.id }
            for (id in saveJobs.keys + drafts) local[id]?.let { incoming[id] = it }
            _notes.value = incoming.values.toList()
            _loading.value = false
            failed.value = false
            serverPending.value = snap.metadata.hasPendingWrites()
        }
    }

    private fun watchNetwork() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        _online.value = cm.activeNetwork != null
        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { _online.value = true }
            override fun onLost(network: Network) { _online.value = cm.activeNetwork != null }
        })
    }

    private fun readGuest(): List<Note> = runCatching {
        if (!guestFile.exists()) return emptyList()
        val arr = JSONArray(guestFile.readText())
        List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            val images = o.optJSONObject("images")?.let { im -> im.keys().asSequence().associateWith { im.getString(it) } } ?: emptyMap()
            Note(o.getString("id"), o.optString("title"), o.optString("body"), o.optLong("date"), o.optString("colour", NO_COLOUR), o.optBoolean("pinned"), images)
        }
    }.getOrDefault(emptyList())

    private fun writeGuest() = runCatching {
        val arr = JSONArray()
        _notes.value.forEach { n ->
            arr.put(JSONObject().put("id", n.id).put("title", n.title).put("body", n.body).put("date", n.date)
                .put("colour", n.colour).put("pinned", n.pinned).put("images", JSONObject(n.images)))
        }
        guestFile.writeText(arr.toString())
    }

    // ---- public API -----------------------------------------------------------------------

    fun note(id: String?): Note? = if (id == null) null else _notes.value.firstOrNull { it.id == id }

    /** Creates an empty note. It is only written once it has content, so blank notes never reach the database. */
    fun create(): Note {
        val note = newNote()
        drafts += note.id
        _notes.value = listOf(note) + _notes.value
        return note
    }

    /** Applies an edit immediately and writes it shortly after. */
    fun update(id: String, touch: Boolean = true, change: (Note) -> Note) {
        _notes.value = _notes.value.map { n ->
            if (n.id != id) n else change(n).let { c ->
                val edited = if (touch) c.copy(date = System.currentTimeMillis()) else c
                // Drop pasted images the text no longer refers to.
                if (edited.images.isEmpty()) edited else edited.copy(images = edited.images.filterKeys { edited.body.contains("img:$it)") })
            }
        }
        if (guest) { writeGuest(); return }
        saveJobs[id]?.cancel()
        saveJobs[id] = scope.launch {
            delay(SAVE_DELAY_MS)
            flush(id)
        }
        localPending.value = saveJobs.size
    }

    fun togglePin(id: String) { note(id)?.let { n -> update(id, touch = false) { it.copy(pinned = !n.pinned) } } }

    fun delete(id: String) {
        saveJobs.remove(id)?.cancel()
        drafts -= id
        localPending.value = saveJobs.size
        _notes.value = _notes.value.filterNot { it.id == id }
        if (guest) { writeGuest(); return }
        email?.let { e -> collection(e).document(id).delete().addOnFailureListener { failed.value = true } }
    }

    /** Drops a freshly created note the user left without typing anything. */
    fun discardIfEmpty(id: String?) {
        val n = note(id) ?: return
        if (n.isBlank && id !in saveJobs) {
            drafts -= n.id
            _notes.value = _notes.value.filterNot { it.id == n.id }
            if (guest) writeGuest()
        }
    }

    private fun flush(id: String) {
        saveJobs.remove(id)
        localPending.value = saveJobs.size
        val n = note(id) ?: return
        if (n.isBlank) { drafts -= id; _notes.value = _notes.value.filterNot { it.id == id }; return }
        drafts -= id
        val e = email ?: return
        val ref: DocumentReference = collection(e).document(id)
        ref.set(n.toMap()).addOnFailureListener { failed.value = true }
    }

    /** Hands every edit that is still waiting out the save delay to the database right now. */
    fun flushAll() {
        saveJobs.keys.toList().forEach { id -> saveJobs[id]?.cancel(); flush(id) }
    }

    /**
     * Saves the last edit and gives the server a moment to confirm it.
     * Returns false when changes are still unsynced afterwards (usually because the device is offline).
     */
    suspend fun settle(timeoutMs: Long = 2500): Boolean {
        if (guest) return true
        flushAll()
        if (!unsynced.value && _online.value) return true
        val confirmed = withTimeoutOrNull(timeoutMs) { db.waitForPendingWrites().await(); true } ?: false
        return confirmed || !unsynced.value
    }
}

package com.srvraj311.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.srvraj311.EnWriteApp
import com.srvraj311.data.Note
import com.srvraj311.data.plainPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Filter { All, Favourites }
enum class Sort(val label: String) { Time("Last edited"), Title("Title"), Colour("Colour") }

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val container = app as EnWriteApp
    val auth = container.auth
    val repo = container.notes
    val settings = container.settings

    val query = MutableStateFlow("")
    val filter = MutableStateFlow(Filter.All)
    val sort = MutableStateFlow(Sort.Time)
    private val _selectedId = MutableStateFlow<String?>(null)
    val selectedId: StateFlow<String?> = _selectedId.asStateFlow()

    private val _toasts = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val toasts = _toasts.asSharedFlow()
    private val _confirmSignOut = MutableStateFlow(false)
    val confirmSignOut: StateFlow<Boolean> = _confirmSignOut.asStateFlow()

    val favouriteCount: StateFlow<Int> = repo.notes.combine(filter) { n, _ -> n.count { it.pinned } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    /** Notes after search, favourites filter and sorting; favourites always float to the top. */
    val visible: StateFlow<List<Note>> = combine(repo.notes, query, filter, sort) { notes, q, f, s ->
        val needle = q.trim().lowercase()
        notes
            .filter { f == Filter.All || it.pinned }
            .filter { needle.isEmpty() || it.title.lowercase().contains(needle) || it.body.lowercase().contains(needle) }
            .sortedWith(
                compareByDescending<Note> { it.pinned }.then(
                    when (s) {
                        Sort.Time -> compareByDescending { it.date }
                        Sort.Title -> compareBy { it.title.ifBlank { "￿" }.lowercase() }
                        Sort.Colour -> compareBy<Note> { it.colour }.thenByDescending { it.date }
                    }
                )
            )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun toast(message: String) { _toasts.tryEmit(message) }

    fun newNote() {
        query.value = ""
        filter.value = Filter.All
        _selectedId.value = repo.create().id
    }

    fun open(id: String) {
        repo.discardIfEmpty(_selectedId.value.takeIf { it != id })
        _selectedId.value = id
    }

    fun close() {
        repo.discardIfEmpty(_selectedId.value)
        _selectedId.value = null
    }

    fun delete(id: String) {
        if (_selectedId.value == id) _selectedId.value = null
        repo.delete(id)
        toast("Note deleted")
    }

    /** Saves the last edit, waits briefly for the server, and asks before leaving if changes are still unsynced. */
    fun requestSignOut() {
        viewModelScope.launch {
            if (repo.unsynced.value) toast("Saving your latest changes...")
            if (repo.settle()) signOutNow() else _confirmSignOut.value = true
        }
    }

    fun dismissSignOutPrompt() { _confirmSignOut.value = false }

    fun signOutNow() {
        _confirmSignOut.value = false
        _selectedId.value = null
        auth.signOut()
    }

    fun snippet(n: Note) = plainPreview(n.body).take(140)
}

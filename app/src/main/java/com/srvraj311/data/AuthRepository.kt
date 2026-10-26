package com.srvraj311.data

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.srvraj311.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class AppUser(
    val uid: String,
    val email: String,
    val name: String,
    val photoUrl: String,
    val emailVerified: Boolean,
    val guest: Boolean,
)

private val GUEST = AppUser("guest", "guest@local", "Guest", "", true, true)

class AuthRepository(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _user = MutableStateFlow<AppUser?>(null)
    private val _ready = MutableStateFlow(false)

    val user: StateFlow<AppUser?> = _user.asStateFlow()
    /** False until Firebase has reported whether a session exists. */
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    init {
        auth.addAuthStateListener { fb ->
            _user.value = fb.currentUser?.toAppUser() ?: if (prefs.getBoolean(GUEST_KEY, false)) GUEST else null
            _ready.value = true
        }
    }

    suspend fun signIn(email: String, password: String) {
        val res = auth.signInWithEmailAndPassword(email.trim(), password).await()
        res.user?.let { saveProfile(it) }
    }

    suspend fun signUp(name: String, email: String, password: String) {
        val res = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val u = res.user ?: return
        u.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()).await()
        runCatching { u.sendEmailVerification().await() }
        saveProfile(u)
        _user.value = auth.currentUser?.toAppUser()
    }

    suspend fun resetPassword(email: String) { auth.sendPasswordResetEmail(email.trim()).await() }

    /** Google sign-in through Credential Manager (the account picker sheet). */
    suspend fun signInWithGoogle(activity: Activity) {
        val option = GetSignInWithGoogleOption.Builder(activity.getString(R.string.default_web_client_id)).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val result = CredentialManager.create(activity).getCredential(activity, request)
        val google = GoogleIdTokenCredential.createFrom(result.credential.data)
        val res = auth.signInWithCredential(GoogleAuthProvider.getCredential(google.idToken, null)).await()
        res.user?.let { saveProfile(it) }
    }

    fun continueAsGuest() {
        prefs.edit().putBoolean(GUEST_KEY, true).apply()
        _user.value = GUEST
    }

    fun signOut() {
        prefs.edit().putBoolean(GUEST_KEY, false).apply()
        auth.signOut()
        _user.value = null
    }

    private fun FirebaseUser.toAppUser() = AppUser(
        uid = uid,
        email = email.orEmpty(),
        name = displayName.orEmpty(),
        photoUrl = photoUrl?.toString().orEmpty(),
        emailVerified = isEmailVerified,
        guest = false,
    )

    /** Same user document the web app writes: users/{email}. */
    private suspend fun saveProfile(u: FirebaseUser) {
        val email = u.email ?: return
        runCatching {
            FirebaseFirestore.getInstance().collection("users").document(email).set(
                mapOf(
                    "uid" to u.uid,
                    "email" to email,
                    "name" to u.displayName,
                    "photoURL" to u.photoUrl?.toString(),
                    "emailVerified" to u.isEmailVerified,
                ),
                SetOptions.merge(),
            ).await()
        }
    }

    companion object {
        private const val GUEST_KEY = "guest"

        /** A friendly message for any sign-in failure. */
        fun message(e: Throwable): String = when (e) {
            is GetCredentialCancellationException -> "Sign-in was cancelled."
            is GetCredentialException -> "Google sign-in is not available right now."
            is FirebaseTooManyRequestsException -> "Too many attempts. Please wait a moment and try again."
            is FirebaseNetworkException -> "Network error. Check your connection."
            is FirebaseAuthException -> when (e.errorCode) {
                "ERROR_USER_NOT_FOUND" -> "No account found with that email."
                "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD" -> "Email or password is incorrect."
                "ERROR_INVALID_EMAIL" -> "That email address does not look right."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "That email is already registered. Try signing in instead."
                "ERROR_WEAK_PASSWORD" -> "Choose a password with at least 6 characters."
                "ERROR_OPERATION_NOT_ALLOWED" -> "This sign-in method is not enabled in the Firebase console."
                else -> "Something went wrong (${e.errorCode})."
            }
            else -> e.message?.takeIf { it.length < 90 } ?: "Something went wrong."
        }
    }
}

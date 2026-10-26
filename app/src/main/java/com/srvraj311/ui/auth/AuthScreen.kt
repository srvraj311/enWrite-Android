package com.srvraj311.ui.auth

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srvraj311.data.AuthRepository
import com.srvraj311.ui.components.AppTextField
import com.srvraj311.ui.components.BrandBrush
import com.srvraj311.ui.components.GradientButton
import com.srvraj311.ui.components.Logo
import com.srvraj311.ui.components.SoftButton
import com.srvraj311.ui.components.Wordmark
import kotlinx.coroutines.launch

private enum class Mode { Login, SignUp, Reset }

@Composable
fun AuthScreen(auth: AuthRepository, onToast: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current

    var mode by remember { mutableStateOf(Mode.Login) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var touched by remember { mutableStateOf(false) }

    val emailOk = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim())
    val passwordOk = password.length >= (if (mode == Mode.SignUp) 6 else 1)
    val nameOk = name.isNotBlank()

    fun go(next: Mode) { mode = next; error = ""; touched = false }

    fun submit() {
        touched = true
        error = ""
        if (!emailOk) return
        if (mode != Mode.Reset && !passwordOk) return
        if (mode == Mode.SignUp && !nameOk) return
        focus.clearFocus()
        busy = true
        scope.launch {
            try {
                when (mode) {
                    Mode.Login -> auth.signIn(email, password)
                    Mode.SignUp -> auth.signUp(name, email, password)
                    Mode.Reset -> { auth.resetPassword(email); onToast("Password reset email sent. Check your inbox."); go(Mode.Login) }
                }
            } catch (e: Exception) {
                error = AuthRepository.message(e)
            } finally {
                busy = false
            }
        }
    }

    fun google() {
        error = ""
        busy = true
        scope.launch {
            try { auth.signInWithGoogle(context as Activity) } catch (e: Exception) { error = AuthRepository.message(e) } finally { busy = false }
        }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Brand header
            Box(
                Modifier.fillMaxWidth().background(BrandBrush).statusBarsPadding().padding(top = 28.dp, bottom = 36.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.clip(RoundedCornerShape(22.dp)).background(Color.White).padding(14.dp)) { Logo(56.dp) }
                    Spacer(Modifier.height(14.dp))
                    Wordmark(40.sp, Color.White)
                    Text("Write fast. Think clearly.", color = Color.White.copy(alpha = .9f), fontSize = 15.sp)
                }
            }

            Column(
                Modifier.widthIn(max = 460.dp).fillMaxWidth().padding(horizontal = 22.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                AnimatedContent(mode, label = "title") { m ->
                    Column {
                        Text(
                            when (m) { Mode.Login -> "Welcome back"; Mode.SignUp -> "Create your account"; Mode.Reset -> "Reset password" },
                            fontSize = 26.sp, fontWeight = FontWeight.Bold,
                        )
                        Text(
                            when (m) {
                                Mode.Login -> "Sign in to pick up where you left off."
                                Mode.SignUp -> "Your notes, synced everywhere in real time."
                                Mode.Reset -> "We will email you a link to choose a new password."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (mode == Mode.SignUp) {
                    AppTextField(name, { name = it }, "Name", leading = Icons.Filled.Person, error = if (touched && !nameOk) "Please enter your name" else null)
                }
                AppTextField(email, { email = it }, "Email", leading = Icons.Filled.Mail, keyboardType = KeyboardType.Email,
                    error = if (touched && !emailOk) "Enter a valid email address" else null)
                if (mode != Mode.Reset) {
                    AppTextField(
                        password, { password = it }, "Password", leading = Icons.Filled.Lock, password = true, showPassword = showPassword,
                        trailing = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, if (showPassword) "Hide password" else "Show password")
                            }
                        },
                        error = if (touched && !passwordOk) (if (mode == Mode.SignUp) "Use at least 6 characters" else "Enter your password") else null,
                    )
                }
                if (mode == Mode.Login) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { go(Mode.Reset) }) { Text("Forgot password?", fontWeight = FontWeight.SemiBold) }
                    }
                }

                if (error.isNotEmpty()) {
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.error.copy(alpha = .12f)).padding(12.dp)) {
                        Text(error, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }
                }

                GradientButton(
                    text = when (mode) { Mode.Login -> "Sign in"; Mode.SignUp -> "Create account"; Mode.Reset -> "Send reset link" },
                    onClick = ::submit, modifier = Modifier.fillMaxWidth(), loading = busy,
                )

                if (mode != Mode.Reset) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outline))
                        Text("or", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outline))
                    }
                    SoftButton(
                        "Continue with Google", onClick = ::google, modifier = Modifier.fillMaxWidth(), enabled = !busy,
                        leading = { Text("G", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary) },
                    )
                    TextButton(onClick = { auth.continueAsGuest() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Try it without an account", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    when (mode) {
                        Mode.Login -> { Text("New to enWrite? ", color = MaterialTheme.colorScheme.onSurfaceVariant); Text("Create an account", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { go(Mode.SignUp) }.padding(8.dp)) }
                        Mode.SignUp -> { Text("Already have an account? ", color = MaterialTheme.colorScheme.onSurfaceVariant); Text("Sign in", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { go(Mode.Login) }.padding(8.dp)) }
                        Mode.Reset -> Text("Back to sign in", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.clickable { go(Mode.Login) }.padding(8.dp))
                    }
                }
            }
        }
    }
}

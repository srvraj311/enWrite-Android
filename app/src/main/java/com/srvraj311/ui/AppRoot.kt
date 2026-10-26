package com.srvraj311.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srvraj311.ui.auth.AuthScreen
import com.srvraj311.ui.components.ConfirmDialog
import com.srvraj311.ui.components.Logo
import com.srvraj311.ui.editor.EditorScreen
import com.srvraj311.ui.home.HomeScreen
import com.srvraj311.ui.settings.SettingsSheet

@Composable
fun AppRoot(vm: MainViewModel = viewModel()) {
    val ready by vm.auth.ready.collectAsState()
    val user by vm.auth.user.collectAsState()
    val selectedId by vm.selectedId.collectAsState()
    val confirmSignOut by vm.confirmSignOut.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.toasts.collect { snackbar.showSnackbar(it) } }
    BackHandler(enabled = selectedId != null) { vm.close() }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
      Box(Modifier.fillMaxSize()) {
        val u = user
        when {
            !ready -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Logo(96.dp) }
            u == null -> AuthScreen(vm.auth, onToast = vm::toast)
            else -> BoxWithConstraints(Modifier.fillMaxSize()) {
                val wide = maxWidth >= 720.dp
                if (wide) {
                    Row(Modifier.fillMaxSize()) {
                        HomeScreen(vm, u, onOpenSettings = { showSettings = true }, modifier = Modifier.width(360.dp).fillMaxHeight())
                        Box(Modifier.weight(1f).fillMaxHeight()) { EditorPane(vm, selectedId, showEmptyHint = true) }
                    }
                } else {
                    AnimatedContent(
                        targetState = selectedId != null,
                        transitionSpec = {
                            if (targetState) (slideInHorizontally { it / 4 } + fadeIn()) togetherWith (fadeOut())
                            else fadeIn() togetherWith (slideOutHorizontally { it / 4 } + fadeOut())
                        },
                        label = "screen",
                    ) { editing ->
                        if (editing) EditorPane(vm, selectedId, showEmptyHint = false)
                        else HomeScreen(vm, u, onOpenSettings = { showSettings = true })
                    }
                }
                if (showSettings) SettingsSheet(vm.settings, u, onSignOut = vm::requestSignOut, onDismiss = { showSettings = false })
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())

        if (confirmSignOut) {
            ConfirmDialog(
                title = "Unsynced changes",
                message = if (vm.repo.online.collectAsState().value)
                    "Some of your latest changes have not finished syncing yet. If you sign out now they may not be saved."
                else
                    "You are offline, so your latest changes are only saved on this device. They will sync the next time you sign in here with this same account.",
                confirmLabel = "Sign out anyway", danger = true,
                onConfirm = vm::signOutNow, onDismiss = vm::dismissSignOutPrompt,
            )
        }
      }
    }
}

@Composable
private fun EditorPane(vm: MainViewModel, selectedId: String?, showEmptyHint: Boolean) {
    EditorScreen(vm, selectedId, showEmptyHint)
}


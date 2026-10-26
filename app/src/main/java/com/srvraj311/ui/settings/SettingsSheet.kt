package com.srvraj311.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srvraj311.BuildConfig
import com.srvraj311.data.AppUser
import com.srvraj311.data.EditorMode
import com.srvraj311.data.SettingsStore
import com.srvraj311.data.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(store: SettingsStore, user: AppUser, onSignOut: () -> Unit, onDismiss: () -> Unit) {
    val settings by store.settings.collectAsState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text("Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            Label("Appearance")
            Segmented(
                items = listOf(Triple(ThemeMode.Light, "Light", Icons.Filled.LightMode), Triple(ThemeMode.Dark, "Dark", Icons.Filled.DarkMode), Triple(ThemeMode.System, "System", Icons.Filled.SettingsBrightness)),
                selected = settings.theme, onSelect = store::setTheme,
            )

            Spacer(Modifier.height(20.dp))
            Label("Open notes in")
            Segmented(
                items = listOf(Triple(EditorMode.Live, "Live", null), Triple(EditorMode.Source, "Source", null), Triple(EditorMode.Preview, "Preview", null)),
                selected = settings.editorMode, onSelect = store::setEditorMode,
            )
            Text(
                "Live shows your Markdown formatted as you type. Source is plain Markdown. Preview is read-only.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp),
            )

            Spacer(Modifier.height(20.dp))
            Label("Account")
            Text(user.name.ifBlank { "Welcome" }, fontWeight = FontWeight.SemiBold)
            Text(if (user.guest) "Guest mode - your notes stay on this device" else user.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.error.copy(alpha = .1f))
                    .clickable { onDismiss(); onSignOut() }.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error)
                Text(if (user.guest) "Leave guest mode" else "Sign out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(20.dp))
            Text("enWrite ${BuildConfig.VERSION_NAME}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text.uppercase(), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun <T> Segmented(items: List<Triple<T, String, ImageVector?>>, selected: T, onSelect: (T) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEach { (value, label, icon) ->
            val on = value == selected
            Row(
                Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(11.dp))
                    .background(if (on) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onSelect(value) },
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) { Icon(icon, null, Modifier.size(18.dp), tint = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.size(6.dp)) }
                Text(label, fontWeight = FontWeight.SemiBold, color = if (on) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        }
    }
}

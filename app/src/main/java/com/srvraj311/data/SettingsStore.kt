package com.srvraj311.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ThemeMode { System, Light, Dark }
enum class EditorMode { Source, Live, Preview }

data class Settings(val theme: ThemeMode = ThemeMode.System, val editorMode: EditorMode = EditorMode.Live)

private val Context.dataStore by preferencesDataStore("enwrite_settings")

class SettingsStore(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val themeKey = stringPreferencesKey("theme")
    private val modeKey = stringPreferencesKey("editor_mode")

    val settings: StateFlow<Settings> = context.dataStore.data
        .map { p ->
            Settings(
                theme = p[themeKey]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.System,
                editorMode = p[modeKey]?.let { runCatching { EditorMode.valueOf(it) }.getOrNull() } ?: EditorMode.Live,
            )
        }
        .stateIn(scope, SharingStarted.Eagerly, Settings())

    fun setTheme(mode: ThemeMode) { scope.launch { context.dataStore.edit { it[themeKey] = mode.name } } }
    fun setEditorMode(mode: EditorMode) { scope.launch { context.dataStore.edit { it[modeKey] = mode.name } } }
}

package com.srvraj311

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.srvraj311.data.ThemeMode
import com.srvraj311.ui.AppRoot
import com.srvraj311.ui.theme.EnWriteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settings = (application as EnWriteApp).settings
        setContent {
            val prefs by settings.settings.collectAsState()
            val dark = when (prefs.theme) {
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
                ThemeMode.System -> isSystemInDarkTheme()
            }
            EnWriteTheme(dark = dark) { AppRoot() }
        }
    }
}

package com.srvraj311.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(
    primary = Pink, onPrimary = LightSurface, secondary = Plum,
    background = LightBg, onBackground = LightText,
    surface = LightSurface, onSurface = LightText,
    surfaceVariant = LightHover, onSurfaceVariant = LightText2,
    surfaceContainer = LightSurface2, outline = LightBorder, outlineVariant = LightBorder,
    error = Danger,
)

private val DarkScheme = darkColorScheme(
    primary = PinkDark, onPrimary = DarkBg, secondary = Plum,
    background = DarkBg, onBackground = DarkText,
    surface = DarkSurface, onSurface = DarkText,
    surfaceVariant = DarkHover, onSurfaceVariant = DarkText2,
    surfaceContainer = DarkSurface2, outline = DarkBorder, outlineVariant = DarkBorder,
    error = DangerDark,
)

@Composable
fun EnWriteTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, content = content)
}

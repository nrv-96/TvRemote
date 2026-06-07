package com.smarttv.remote.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Teal,
    secondary = AccentBlue,
    tertiary = AccentOrange,
    background = DarkBackground,
    surface = DarkSurface,
    error = ErrorRed,
    onPrimary = LightSurface,
    onSecondary = LightSurface,
    onBackground = LightSurface,
    onSurface = LightSurface,
    onError = LightSurface
)

private val LightColorScheme = lightColorScheme(
    primary = Teal,
    secondary = AccentBlue,
    tertiary = AccentOrange,
    background = LightBackground,
    surface = LightSurface,
    error = ErrorRed,
    onPrimary = LightSurface,
    onSecondary = LightSurface,
    onBackground = DarkBackground,
    onSurface = DarkBackground,
    onError = LightSurface
)

@Composable
fun TvRemoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisDarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = DarkBackground,
    primaryContainer = BlueDark,
    onPrimaryContainer = CyanPrimary,
    secondary = BlueAccent,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceGlow,
    onSecondaryContainer = TextPrimary,
    tertiary = AmberAccent,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = HologramBorder,
    error = RedAlert,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // JARVIS is inherently a dark futuristic HUD
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = JarvisDarkColorScheme,
        typography = Typography,
        content = content
    )
}


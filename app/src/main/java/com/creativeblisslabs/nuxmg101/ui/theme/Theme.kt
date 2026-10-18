package com.creativeblisslabs.nuxmg101.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- Creamy Light Theme Palette Constants ---
val CreamBackground = Color(0xFFFDFBF7)
val CreamSurface = Color(0xFFF7F4EE)
val CreamSurfaceVariant = Color(0xFFEEE8DD)
val CreamSurfaceLowest = Color(0xFFFFFFFF)
val CreamSurfaceLow = Color(0xFFFAF7F2)
val CreamSurfaceHigh = Color(0xFFE5DDD0)
val CreamSurfaceHighest = Color(0xFFDDD3C3)
val CreamSurfaceBright = Color(0xFFFFFFFF)

val CreamTextOnSurface = Color(0xFF2C2622)
val CreamTextOnSurfaceVariant = Color(0xFF6B5F56)

val CreamPrimary = Color(0xFFB55839)       // Warm Terracotta / Burnt Orange
val CreamOnPrimary = Color(0xFFFFFFFF)
val CreamPrimaryContainer = Color(0xFFFFDBCE)
val CreamOnPrimaryContainer = Color(0xFF3C0C00)

val CreamSecondary = Color(0xFF7A6B53)     // Muted Warm Bronze / Olive Gray
val CreamSecondaryContainer = Color(0xFFEDE4D5)
val CreamOnSecondaryContainer = Color(0xFF272115)

val CreamTertiary = Color(0xFF5A7263)      // Soft Muted Sage
val CreamOutline = Color(0xFFB8AD9E)
val CreamOutlineVariant = Color(0xFFD6CDBF)

private val DarkStudioColorScheme = darkColorScheme(
    primary = CreamPrimary,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = StudioMintPrimary,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = StudioAmberSecondary,
    background = DarkBackground,
    onBackground = TextOnSurfaceDark,
    surface = DarkBackground,
    onSurface = TextOnSurfaceDark,
    surfaceVariant = SurfaceContainerHighestDark,
    onSurfaceVariant = TextOnSurfaceVariantDark,
    outline = BorderOutlineDark,
    outlineVariant = BorderOutlineVariantDark,
    error = StatusError,
    errorContainer = StatusErrorContainer,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDefaultDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    surfaceBright = SurfaceBrightDark
)

private val LightStudioColorScheme = lightColorScheme(
    primary = CreamPrimary,
    onPrimary = CreamOnPrimary,
    primaryContainer = CreamPrimaryContainer,
    onPrimaryContainer = CreamOnPrimaryContainer,
    secondary = CreamSecondary,
    onSecondary = Color.White,
    secondaryContainer = CreamSecondaryContainer,
    onSecondaryContainer = CreamOnSecondaryContainer,
    tertiary = CreamTertiary,
    background = CreamBackground,
    onBackground = CreamTextOnSurface,
    surface = CreamSurface,
    onSurface = CreamTextOnSurface,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = CreamTextOnSurfaceVariant,
    outline = CreamOutline,
    outlineVariant = CreamOutlineVariant,
    error = StatusError,
    errorContainer = StatusErrorContainer,
    surfaceContainerLowest = CreamSurfaceLowest,
    surfaceContainerLow = CreamSurfaceLow,
    surfaceContainer = CreamSurface,
    surfaceContainerHigh = CreamSurfaceHigh,
    surfaceContainerHighest = CreamSurfaceHighest,
    surfaceBright = CreamSurfaceBright
)

@Composable
fun QuickToneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkStudioColorScheme else LightStudioColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
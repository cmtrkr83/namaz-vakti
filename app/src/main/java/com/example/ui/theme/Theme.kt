package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = EmeraldGradTop,
    primaryContainer = GoldMuted,
    onPrimaryContainer = GoldLight,
    secondary = EmeraldAccent,
    onSecondary = EmeraldGradTop,
    secondaryContainer = EmeraldPrimary,
    onSecondaryContainer = MintSoft,
    tertiary = KerahatAmber,
    background = EmeraldGradTop,
    onBackground = TextPrimaryDark,
    surface = EmeraldCardSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = EmeraldCardVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = EmeraldBorder
)

private val LightColorScheme = lightColorScheme(
    primary = GoldDark,
    onPrimary = Color.White,
    primaryContainer = GoldLight,
    onPrimaryContainer = EmeraldGradTop,
    secondary = EmeraldPrimary,
    onSecondary = Color.White,
    secondaryContainer = MintSoft,
    onSecondaryContainer = EmeraldGradTop,
    tertiary = KerahatAmber,
    background = SageBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = CardSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = CardSurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderSubtleLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to serene dark emerald theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                WindowCompat.setDecorFitsSystemWindows(it, false)
                it.statusBarColor = Color.Transparent.toArgb()
                it.navigationBarColor = Color.Transparent.toArgb()
                val controller = WindowCompat.getInsetsController(it, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

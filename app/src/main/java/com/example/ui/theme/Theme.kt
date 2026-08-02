package com.example.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

private val MuftToolsDarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = TextPrimary,
    secondary = VioletSecondary,
    onSecondary = TextPrimary,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = EmeraldTertiary,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

@Composable
fun MuftToolsTheme(
    darkTheme: Boolean = true, // Default to Material 3 Dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = MuftToolsDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            runCatching {
                val activity = view.context.findActivity()
                if (activity != null) {
                    val window = activity.window
                    @Suppress("DEPRECATION")
                    window.statusBarColor = DarkBackground.toArgb()
                    @Suppress("DEPRECATION")
                    window.navigationBarColor = DarkBackground.toArgb()
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = false
                        isAppearanceLightNavigationBars = false
                    }
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias if needed
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    MuftToolsTheme(darkTheme = true, content = content)
}

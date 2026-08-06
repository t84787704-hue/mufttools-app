package com.example.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
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
    primary = CyanPrimaryDark,
    onPrimary = DarkBackgroundStatic,
    primaryContainer = DarkSurfaceVariantStatic,
    onPrimaryContainer = DarkTextPrimaryStatic,
    secondary = VioletSecondary,
    onSecondary = DarkTextPrimaryStatic,
    secondaryContainer = DarkSurfaceVariantStatic,
    onSecondaryContainer = DarkTextPrimaryStatic,
    tertiary = EmeraldTertiary,
    onTertiary = DarkBackgroundStatic,
    background = DarkBackgroundStatic,
    onBackground = DarkTextPrimaryStatic,
    surface = DarkSurfaceStatic,
    onSurface = DarkTextPrimaryStatic,
    surfaceVariant = DarkSurfaceVariantStatic,
    onSurfaceVariant = DarkTextSecondaryStatic,
    outline = DarkTextMutedStatic,
    outlineVariant = DarkCardBorderStatic
)

private val MuftToolsLightColorScheme = lightColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceVariantStatic,
    onPrimaryContainer = LightTextPrimaryStatic,
    secondary = VioletSecondary,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariantStatic,
    onSecondaryContainer = LightTextPrimaryStatic,
    tertiary = EmeraldTertiary,
    onTertiary = Color.White,
    background = LightBackgroundStatic,
    onBackground = LightTextPrimaryStatic,
    surface = LightSurfaceStatic,
    onSurface = LightTextPrimaryStatic,
    surfaceVariant = LightSurfaceVariantStatic,
    onSurfaceVariant = LightTextSecondaryStatic,
    outline = LightTextMutedStatic,
    outlineVariant = LightCardBorderStatic
)

@Composable
fun MuftToolsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MuftToolsDarkColorScheme
        else -> MuftToolsLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            runCatching {
                val activity = view.context.findActivity()
                if (activity != null) {
                    val window = activity.window
                    @Suppress("DEPRECATION")
                    window.statusBarColor = colorScheme.background.toArgb()
                    @Suppress("DEPRECATION")
                    window.navigationBarColor = colorScheme.background.toArgb()
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
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

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MuftToolsTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}


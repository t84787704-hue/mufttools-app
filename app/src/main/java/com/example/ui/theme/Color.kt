package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Static Dark theme colors
val DarkBackgroundStatic = Color(0xFF0F172A)
val DarkSurfaceStatic = Color(0xFF1E293B)
val DarkSurfaceVariantStatic = Color(0xFF334155)

// Static Light theme colors
val LightBackgroundStatic = Color(0xFFF8FAFC)
val LightSurfaceStatic = Color(0xFFFFFFFF)
val LightSurfaceVariantStatic = Color(0xFFE2E8F0)

val CyanPrimary = Color(0xFF0284C7)
val CyanPrimaryDark = Color(0xFF38BDF8)
val VioletSecondary = Color(0xFF8B5CF6)
val VioletGlowing = Color(0xFF9D4EDD)
val VioletButton = Color(0xFF7B2CBF)
val CrownGold = Color(0xFFFFD700)
val EmeraldTertiary = Color(0xFF10B981)

val AccentAmber = Color(0xFFF59E0B)
val AccentRose = Color(0xFFF43F5E)
val AccentBlue = Color(0xFF3B82F6)

// Static Text colors
val DarkTextPrimaryStatic = Color(0xFFF8FAFC)
val DarkTextSecondaryStatic = Color(0xFF94A3B8)
val DarkTextMutedStatic = Color(0xFF64748B)

val LightTextPrimaryStatic = Color(0xFF0F172A)
val LightTextSecondaryStatic = Color(0xFF475569)
val LightTextMutedStatic = Color(0xFF64748B)

val DarkCardBorderStatic = Color(0xFF475569)
val LightCardBorderStatic = Color(0xFFCBD5E1)

// Dynamic Composable Theme-Aware Color Accessors
val DarkBackground: Color
    @Composable
    get() = MaterialTheme.colorScheme.background

val DarkSurface: Color
    @Composable
    get() = MaterialTheme.colorScheme.surface

val DarkSurfaceVariant: Color
    @Composable
    get() = MaterialTheme.colorScheme.surfaceVariant

val TextPrimary: Color
    @Composable
    get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val TextMuted: Color
    @Composable
    get() = MaterialTheme.colorScheme.outline

val CardBorder: Color
    @Composable
    get() = MaterialTheme.colorScheme.outlineVariant

val CardBackground: Color
    @Composable
    get() = MaterialTheme.colorScheme.surface


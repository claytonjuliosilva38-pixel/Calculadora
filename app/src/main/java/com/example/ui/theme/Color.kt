package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Default Accent (Amber)
val AmberAccent = Color(0xFFFF9F0A)
val AmberAccentInk = Color(0xFF221A08)

// Light Theme Tokens
val LightBg = Color(0xFFEEF1F6)
val LightCard = Color(0xFFFFFFFF)
val LightLine = Color(0xFFE3E7EE)
val LightText = Color(0xFF171A21)
val LightMuted = Color(0xFF687182)
val LightKey = Color(0xFFF6F7FA)
val LightKeyEdge = Color(0xFFD9DEE8)
val LightKeyText = Color(0xFF1C212B)
val LightFn = Color(0xFFE9EDF4)
val LightFnEdge = Color(0xFFD2D8E4)
val LightDispBg = Color(0xFFF7F9FC)
val LightChip = Color(0xFFFFFFFF)

// Dark Theme Tokens
val DarkBg = Color(0xFF0B0E13)
val DarkCard = Color(0xFF141924)
val DarkLine = Color(0xFF232B3A)
val DarkText = Color(0xFFF2F4F8)
val DarkMuted = Color(0xFF8B95A9)
val DarkKey = Color(0xFF1E2532)
val DarkKeyEdge = Color(0xFF11151E)
val DarkKeyText = Color(0xFFEEF1F6)
val DarkFn = Color(0xFF252D3D)
val DarkFnEdge = Color(0xFF161B26)
val DarkDispBg = Color(0xFF10141D)
val DarkChip = Color(0xFF1A2130)

val DangerRed = Color(0xFFE5484D)

@Immutable
data class TeclaColors(
    val bg: Color,
    val card: Color,
    val line: Color,
    val text: Color,
    val muted: Color,
    val key: Color,
    val keyEdge: Color,
    val keyText: Color,
    val fn: Color,
    val fnEdge: Color,
    val dispBg: Color,
    val chip: Color,
    val accent: Color,
    val accentInk: Color,
    val danger: Color = DangerRed,
    val isDark: Boolean
)

val LocalTeclaColors = staticCompositionLocalOf {
    TeclaColors(
        bg = DarkBg,
        card = DarkCard,
        line = DarkLine,
        text = DarkText,
        muted = DarkMuted,
        key = DarkKey,
        keyEdge = DarkKeyEdge,
        keyText = DarkKeyText,
        fn = DarkFn,
        fnEdge = DarkFnEdge,
        dispBg = DarkDispBg,
        chip = DarkChip,
        accent = AmberAccent,
        accentInk = AmberAccentInk,
        danger = DangerRed,
        isDark = true
    )
}

fun calculateInkColor(accent: Color): Color {
    // Luminance approximation for contrast
    val r = accent.red * 255.0
    val g = accent.green * 255.0
    val b = accent.blue * 255.0
    val lum = (r * 299.0 + g * 587.0 + b * 114.0) / 1000.0
    return if (lum > 155.0) Color(0xFF15181E) else Color(0xFFFFFFFF)
}

package com.example.model

import androidx.compose.ui.graphics.Color

enum class AngleMode {
    DEG,
    RAD
}

enum class ThemePreference {
    LIGHT,
    DARK,
    SYSTEM
}

data class HistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val expression: String,
    val result: String,
    val rawValue: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class AccentOption(
    val name: String,
    val color: Color,
    val hexString: String
)

val PRESET_ACCENTS = listOf(
    AccentOption("Âmbar", Color(0xFFFF9F0A), "#FF9F0A"),
    AccentOption("Coral", Color(0xFFFF5C5C), "#FF5C5C"),
    AccentOption("Rosa", Color(0xFFFF4F9A), "#FF4F9A"),
    AccentOption("Verde", Color(0xFF2FBF71), "#2FBF71"),
    AccentOption("Teal", Color(0xFF14B8A6), "#14B8A6"),
    AccentOption("Azul", Color(0xFF4F8CFF), "#4F8CFF"),
    AccentOption("Violeta", Color(0xFF9D6BFF), "#9D6BFF"),
    AccentOption("Grafite", Color(0xFF8A93A6), "#8A93A6")
)

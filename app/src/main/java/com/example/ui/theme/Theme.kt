package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.example.model.ThemePreference

object TeclaTheme {
    val colors: TeclaColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTeclaColors.current
}

@Composable
fun TeclaAppTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    accentColor: Color = AmberAccent,
    content: @Composable () -> Unit
) {
    val isDark = when (themePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }

    val accentInk = calculateInkColor(accentColor)

    val teclaColors = if (isDark) {
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
            accent = accentColor,
            accentInk = accentInk,
            danger = DangerRed,
            isDark = true
        )
    } else {
        TeclaColors(
            bg = LightBg,
            card = LightCard,
            line = LightLine,
            text = LightText,
            muted = LightMuted,
            key = LightKey,
            keyEdge = LightKeyEdge,
            keyText = LightKeyText,
            fn = LightFn,
            fnEdge = LightFnEdge,
            dispBg = LightDispBg,
            chip = LightChip,
            accent = accentColor,
            accentInk = accentInk,
            danger = DangerRed,
            isDark = false
        )
    }

    val materialColors = if (isDark) {
        darkColorScheme(
            primary = accentColor,
            onPrimary = accentInk,
            background = DarkBg,
            onBackground = DarkText,
            surface = DarkCard,
            onSurface = DarkText,
            surfaceVariant = DarkDispBg,
            onSurfaceVariant = DarkMuted,
            outline = DarkLine
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            onPrimary = accentInk,
            background = LightBg,
            onBackground = LightText,
            surface = LightCard,
            onSurface = LightText,
            surfaceVariant = LightDispBg,
            onSurfaceVariant = LightMuted,
            outline = LightLine
        )
    }

    CompositionLocalProvider(LocalTeclaColors provides teclaColors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = Typography,
            content = content
        )
    }
}

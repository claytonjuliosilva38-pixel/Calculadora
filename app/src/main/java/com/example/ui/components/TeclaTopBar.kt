package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ThemePreference
import com.example.ui.theme.TeclaTheme

@Composable
fun TeclaTopBar(
    themePreference: ThemePreference,
    onToggleTheme: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = TeclaTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // App Logo Badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = colors.accent.copy(alpha = 0.5f),
                        spotColor = colors.accent.copy(alpha = 0.5f)
                    )
                    .background(colors.accent, RoundedCornerShape(12.dp))
                    .testTag("brand_logo"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "=",
                    color = colors.accentInk,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text(
                    text = "TECLA",
                    color = colors.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "calculadora científica",
                    color = colors.muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Top Action Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme Toggle Button
            val themeIcon = when (themePreference) {
                ThemePreference.LIGHT -> Icons.Default.LightMode
                ThemePreference.DARK -> Icons.Default.DarkMode
                ThemePreference.SYSTEM -> Icons.Default.BrightnessAuto
            }
            val themeLabel = when (themePreference) {
                ThemePreference.LIGHT -> "Tema claro"
                ThemePreference.DARK -> "Tema escuro"
                ThemePreference.SYSTEM -> "Tema do sistema"
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.chip)
                    .border(1.dp, colors.line, RoundedCornerShape(12.dp))
                    .clickable(onClick = onToggleTheme)
                    .testTag("btn_theme"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = themeIcon,
                    contentDescription = themeLabel,
                    tint = colors.muted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Settings & History Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.chip)
                    .border(1.dp, colors.line, RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenSettings)
                    .testTag("btn_settings"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Ajustes e histórico",
                    tint = colors.muted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

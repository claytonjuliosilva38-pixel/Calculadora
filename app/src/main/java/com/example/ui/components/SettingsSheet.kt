package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AccentOption
import com.example.model.HistoryItem
import com.example.model.PRESET_ACCENTS
import com.example.model.ThemePreference
import com.example.ui.theme.TeclaTheme
import com.example.ui.theme.calculateInkColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    themePreference: ThemePreference,
    onSelectTheme: (ThemePreference) -> Unit,
    currentAccentHex: String,
    onSelectAccent: (AccentOption) -> Unit,
    history: List<HistoryItem>,
    onClearHistory: () -> Unit,
    onRestoreHistory: (HistoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val colors = TeclaTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.card,
        contentColor = colors.text,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(colors.line)
            )
        },
        modifier = modifier.testTag("settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AJUSTES",
                    color = colors.muted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_close_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = colors.muted
                    )
                }
            }

            // Section 1: Theme Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(title = "Tema")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.fn)
                        .border(1.dp, colors.line, RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ThemeSegmentButton(
                        text = "Claro",
                        icon = Icons.Default.LightMode,
                        selected = themePreference == ThemePreference.LIGHT,
                        onClick = { onSelectTheme(ThemePreference.LIGHT) },
                        modifier = Modifier.weight(1f),
                        tag = "seg_theme_light"
                    )
                    ThemeSegmentButton(
                        text = "Escuro",
                        icon = Icons.Default.DarkMode,
                        selected = themePreference == ThemePreference.DARK,
                        onClick = { onSelectTheme(ThemePreference.DARK) },
                        modifier = Modifier.weight(1f),
                        tag = "seg_theme_dark"
                    )
                    ThemeSegmentButton(
                        text = "Sistema",
                        icon = Icons.Default.BrightnessAuto,
                        selected = themePreference == ThemePreference.SYSTEM,
                        onClick = { onSelectTheme(ThemePreference.SYSTEM) },
                        modifier = Modifier.weight(1f),
                        tag = "seg_theme_system"
                    )
                }
            }

            // Section 2: Accent Color Swatches
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(title = "Cor das teclas")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.fn)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = currentAccentHex,
                            color = colors.muted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Swatch Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PRESET_ACCENTS.take(4).forEach { option ->
                        ColorSwatch(
                            option = option,
                            isSelected = option.hexString.equals(currentAccentHex, ignoreCase = true),
                            onSelect = { onSelectAccent(option) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PRESET_ACCENTS.drop(4).take(4).forEach { option ->
                        ColorSwatch(
                            option = option,
                            isSelected = option.hexString.equals(currentAccentHex, ignoreCase = true),
                            onSelect = { onSelectAccent(option) }
                        )
                    }
                }
            }

            // Section 3: History List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(title = "Histórico")
                    if (history.isNotEmpty()) {
                        TextButton(
                            onClick = onClearHistory,
                            modifier = Modifier.testTag("btn_clear_history")
                        ) {
                            Text(
                                text = "Limpar",
                                color = colors.danger,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                color = colors.line,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum cálculo ainda.\nSeus resultados aparecem aqui.",
                            color = colors.muted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(history, key = { it.id }) { item ->
                            HistoryCard(
                                item = item,
                                onClick = { onRestoreHistory(item) }
                            )
                        }
                    }
                }
            }

            // Hints / Tips
            Text(
                text = "Porcentagem no padrão de calculadora: 20 + 50% = 30 (acréscimo) e 20 − 50% = 10 (desconto).",
                color = colors.muted,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    val colors = TeclaTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.accent)
        )
        Text(
            text = title.uppercase(),
            color = colors.muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
fun ThemeSegmentButton(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    val colors = TeclaTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.card else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) colors.text else colors.muted,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                color = if (selected) colors.text else colors.muted,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun ColorSwatch(
    option: AccentOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val colors = TeclaTheme.colors
    val ink = calculateInkColor(option.color)

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(option.color)
            .clickable(onClick = onSelect)
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, colors.card, CircleShape)
                } else {
                    Modifier
                }
            )
            .testTag("swatch_${option.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selecionado",
                tint = ink,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun HistoryCard(
    item: HistoryItem,
    onClick: () -> Unit
) {
    val colors = TeclaTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.fn)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("history_item_${item.id}"),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "${item.expression} =",
            color = colors.muted,
            fontSize = 12.sp
        )
        Text(
            text = item.result,
            color = colors.text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

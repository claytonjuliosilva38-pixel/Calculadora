package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AngleMode
import com.example.ui.theme.TeclaTheme
import kotlinx.coroutines.delay

@Composable
fun TeclaDisplay(
    expressionText: String,
    mainText: String,
    memory: Double,
    angleMode: AngleMode,
    shakeTrigger: Long,
    popTrigger: Long,
    onMemOp: (String) -> Unit,
    onToggleAngle: () -> Unit,
    onCopyResult: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = TeclaTheme.colors
    val clipboardManager = LocalClipboardManager.current

    // Horizontal scroll states for long formulas / numbers
    val exprScrollState = rememberScrollState()
    val mainScrollState = rememberScrollState()

    // Auto-scroll to end when text updates
    LaunchedEffect(expressionText) {
        exprScrollState.scrollTo(exprScrollState.maxValue)
    }
    LaunchedEffect(mainText) {
        mainScrollState.scrollTo(mainScrollState.maxValue)
    }

    // Shake animation state on error
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -16f at 80
                    16f at 160
                    -10f at 240
                    10f at 320
                    0f at 400
                }
            )
        }
    }

    // Pop animation on evaluation
    val popScale = remember { Animatable(1f) }
    LaunchedEffect(popTrigger) {
        if (popTrigger > 0) {
            popScale.snapTo(0.92f)
            popScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    // Font size scaling based on text length
    val textLength = mainText.length
    val fontSize = when {
        textLength <= 9 -> 44.sp
        textLength <= 13 -> 34.sp
        textLength <= 17 -> 26.sp
        else -> 20.sp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .graphicsLayer {
                translationX = shakeOffset.value
            }
            .clip(RoundedCornerShape(24.dp))
            .background(colors.dispBg)
            .border(1.dp, colors.line, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("calculator_display")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Chips row: MC, MR, M-, M+, Memory Badge, DEG/RAD Badge, Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val hasMemory = memory != 0.0

                MemoryChip(
                    text = "MC",
                    enabled = hasMemory,
                    onClick = { onMemOp("mc") },
                    tag = "chip_mc"
                )
                MemoryChip(
                    text = "MR",
                    enabled = hasMemory,
                    onClick = { onMemOp("mr") },
                    tag = "chip_mr"
                )
                MemoryChip(
                    text = "M−",
                    enabled = true,
                    onClick = { onMemOp("mminus") },
                    tag = "chip_mminus"
                )
                MemoryChip(
                    text = "M+",
                    enabled = true,
                    onClick = { onMemOp("mplus") },
                    tag = "chip_mplus"
                )

                Spacer(modifier = Modifier.weight(1f))

                // Memory Indicator Badge
                AnimatedVisibility(
                    visible = hasMemory,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("badge_memory"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            color = colors.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Angle Badge (DEG / RAD)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.accent.copy(alpha = 0.15f))
                        .clickable(onClick = onToggleAngle)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                        .testTag("badge_angle"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = angleMode.name,
                        color = colors.accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Copy Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.chip)
                        .border(1.dp, colors.line, RoundedCornerShape(8.dp))
                        .clickable {
                            clipboardManager.setText(AnnotatedString(mainText))
                            onCopyResult("Copiado: $mainText")
                        }
                        .testTag("btn_copy"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar resultado",
                        tint = colors.muted,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Small expression line (formula preview or prior evaluation)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 22.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = expressionText.ifEmpty { " " },
                    color = colors.muted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(exprScrollState)
                        .testTag("display_expression")
                )
            }

            // Big main result / current number
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = mainText,
                    color = colors.text,
                    fontSize = fontSize,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = popScale.value
                            scaleY = popScale.value
                        }
                        .horizontalScroll(mainScrollState)
                        .testTag("display_main")
                )
            }
        }
    }
}

@Composable
fun MemoryChip(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val colors = TeclaTheme.colors

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.chip.copy(alpha = if (enabled) 1f else 0.4f))
            .border(
                width = 1.dp,
                color = colors.line.copy(alpha = if (enabled) 1f else 0.4f),
                shape = RoundedCornerShape(999.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 4.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = colors.muted.copy(alpha = if (enabled) 1f else 0.4f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

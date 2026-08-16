package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AngleMode
import com.example.ui.theme.TeclaTheme

enum class KeyKind {
    DIGIT,
    OPERATOR,
    EQUALS,
    FUNCTION,
    DANGER,
    SCIENTIFIC
}

@Composable
fun TeclaKeypad(
    isScientificOpen: Boolean,
    isSecond: Boolean,
    angleMode: AngleMode,
    onToggleScientific: () -> Unit,
    onToggleSecond: () -> Unit,
    onToggleAngle: () -> Unit,
    onDigit: (String) -> Unit,
    onDot: () -> Unit,
    onOp: (String) -> Unit,
    onFunc: (String) -> Unit,
    onConst: (String) -> Unit,
    onPost: (String) -> Unit,
    onParen: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onNeg: () -> Unit,
    onPct: () -> Unit,
    onPow: () -> Unit,
    onInv: () -> Unit,
    onExp: () -> Unit,
    onTenX: () -> Unit,
    onEquals: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = TeclaTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Scientific Section Toggle Bar
        val chevronRotation by animateFloatAsState(
            targetValue = if (isScientificOpen) 180f else 0f,
            animationSpec = tween(durationMillis = 300),
            label = "chevron_rot"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleScientific)
                .padding(vertical = 4.dp)
                .testTag("btn_toggle_scientific"),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isScientificOpen) "OCULTAR" else "CIENTÍFICA",
                color = colors.muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expandir científica",
                tint = colors.muted,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(chevronRotation)
            )
        }

        // Collapsible Scientific Keypad (4 rows x 4 columns)
        AnimatedVisibility(
            visible = isScientificOpen,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sci Row 1: 2nd, DEG/RAD, (, )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TeclaKey(
                        text = "2nd",
                        kind = KeyKind.SCIENTIFIC,
                        isActive = isSecond,
                        onClick = onToggleSecond,
                        tag = "key_2nd",
                        modifier = Modifier.weight(1f)
                    )
                    TeclaKey(
                        text = angleMode.name,
                        kind = KeyKind.SCIENTIFIC,
                        onClick = onToggleAngle,
                        tag = "key_deg_rad",
                        modifier = Modifier.weight(1f)
                    )
                    TeclaKey(
                        text = "(",
                        kind = KeyKind.SCIENTIFIC,
                        onClick = { onParen("(") },
                        tag = "key_paren_open",
                        modifier = Modifier.weight(1f)
                    )
                    TeclaKey(
                        text = ")",
                        kind = KeyKind.SCIENTIFIC,
                        onClick = { onParen(")") },
                        tag = "key_paren_close",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Sci Row 2: sin/asin, cos/acos, tan/atan, !
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TeclaKey(
                        text = if (isSecond) "sin⁻¹" else "sin",
                        kind = KeyKind.SCIENTIFIC,
                        onClick = { onFunc(if (isSecond) "asin" else "sin") },
                        tag = "key_sin",
                        modifier = Modifier.weight(1f)
                    )
                    TeclaKey(
                        text = if (isSecond) "cos⁻¹" else "cos",
                        kind = KeyKind.SCIENTIFIC,
                        onClick = { onFunc(if (isSecond) "acos" else "cos") },
                        tag = "key_cos",
                        modifier = Modifier.weight(1f)
                    )
                    TeclaKey(
                        text = if (isSecond) "tan⁻¹" else "tan",
                        kind = KeyKind.SCIENTIFIC,
                        onClick = { onFunc(if (isSecond) "atan" else "tan") },
                        tag = "key_tan",
                        modifier = Modifier.weight(1f)
                    )
                    TeclaKey(
                        text = "!",
                        kind = KeyKind.SCIENTIFIC,
                        onClick = { onPost("!") },
                        tag = "key_fact",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Sci Row 3: x²/x³, xʸ, √x/∛x, 1/x
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TeclaKey(
                        text = if (isSecond) "x³" else "x²",
                        kind = KeyKind.SCIENTIFIC,
                        onClick = { onPost(if (isSecond) "³" else "²") },
                        tag = "key_sq",
                        modifier = Modifier.weight(1f)
                    )
                    TeclaKey(
                        text = "xʸ",
                        kind = KeyKind.SCIENTIFIC,
                        onClick = onPow,
                        tag = "key_pow",
                        modifier = Modifier.weight(1f)
                    )
                    TeclaKey(
                        text = if (isSecond) "∛x" else "√x",
                        kind = KeyKind.SCIENTIFIC,
                        onClick = { onFunc(if (isSecond) "cbrt" else "sqrt") },
                        tag = "key_sqrt",
                        modifier = Modifier.weight(1f)
                    )
                    TeclaKey(
                        text = "1/x",
                        kind = KeyKind.SCIENTIFIC,
                        onClick = onInv,
                        tag = "key_inv",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Sci Row 4: π, e, ln/eˣ, log/10ˣ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TeclaKey(
                        text = "π",
                        kind = KeyKind.SCIENTIFIC,
                        onClick = { onConst("π") },
                        tag = "key_pi",
                        modifier = Modifier.weight(1f)
                    )
                    TeclaKey(
                        text = "e",
                        kind = KeyKind.SCIENTIFIC,
                        onClick = { onConst("e") },
                        tag = "key_e",
                        modifier = Modifier.weight(1f)
                    )
                    TeclaKey(
                        text = if (isSecond) "eˣ" else "ln",
                        kind = KeyKind.SCIENTIFIC,
                        onClick = { if (isSecond) onExp() else onFunc("ln") },
                        tag = "key_ln",
                        modifier = Modifier.weight(1f)
                    )
                    TeclaKey(
                        text = if (isSecond) "10ˣ" else "log",
                        kind = KeyKind.SCIENTIFIC,
                        onClick = { if (isSecond) onTenX() else onFunc("log") },
                        tag = "key_log",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Standard 5x4 Keypad Grid
        // Row 1: C, ⌫, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TeclaKey(
                text = "C",
                kind = KeyKind.DANGER,
                onClick = onClear,
                tag = "key_clear",
                modifier = Modifier.weight(1f)
            )
            TeclaKey(
                text = "⌫",
                kind = KeyKind.FUNCTION,
                onClick = onBack,
                tag = "key_back",
                modifier = Modifier.weight(1f)
            )
            TeclaKey(
                text = "%",
                kind = KeyKind.FUNCTION,
                onClick = onPct,
                tag = "key_pct",
                modifier = Modifier.weight(1f)
            )
            TeclaKey(
                text = "÷",
                kind = KeyKind.OPERATOR,
                onClick = { onOp("÷") },
                tag = "key_div",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TeclaKey(
                text = "7",
                kind = KeyKind.DIGIT,
                onClick = { onDigit("7") },
                tag = "key_7",
                modifier = Modifier.weight(1f)
            )
            TeclaKey(
                text = "8",
                kind = KeyKind.DIGIT,
                onClick = { onDigit("8") },
                tag = "key_8",
                modifier = Modifier.weight(1f)
            )
            TeclaKey(
                text = "9",
                kind = KeyKind.DIGIT,
                onClick = { onDigit("9") },
                tag = "key_9",
                modifier = Modifier.weight(1f)
            )
            TeclaKey(
                text = "×",
                kind = KeyKind.OPERATOR,
                onClick = { onOp("×") },
                tag = "key_mul",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TeclaKey(
                text = "4",
                kind = KeyKind.DIGIT,
                onClick = { onDigit("4") },
                tag = "key_4",
                modifier = Modifier.weight(1f)
            )
            TeclaKey(
                text = "5",
                kind = KeyKind.DIGIT,
                onClick = { onDigit("5") },
                tag = "key_5",
                modifier = Modifier.weight(1f)
            )
            TeclaKey(
                text = "6",
                kind = KeyKind.DIGIT,
                onClick = { onDigit("6") },
                tag = "key_6",
                modifier = Modifier.weight(1f)
            )
            TeclaKey(
                text = "−",
                kind = KeyKind.OPERATOR,
                onClick = { onOp("−") },
                tag = "key_sub",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TeclaKey(
                text = "1",
                kind = KeyKind.DIGIT,
                onClick = { onDigit("1") },
                tag = "key_1",
                modifier = Modifier.weight(1f)
            )
            TeclaKey(
                text = "2",
                kind = KeyKind.DIGIT,
                onClick = { onDigit("2") },
                tag = "key_2",
                modifier = Modifier.weight(1f)
            )
            TeclaKey(
                text = "3",
                kind = KeyKind.DIGIT,
                onClick = { onDigit("3") },
                tag = "key_3",
                modifier = Modifier.weight(1f)
            )
            TeclaKey(
                text = "+",
                kind = KeyKind.OPERATOR,
                onClick = { onOp("+") },
                tag = "key_add",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 5: ±, 0, ,, =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TeclaKey(
                text = "±",
                kind = KeyKind.FUNCTION,
                onClick = onNeg,
                tag = "key_neg",
                modifier = Modifier.weight(1f)
            )
            TeclaKey(
                text = "0",
                kind = KeyKind.DIGIT,
                onClick = { onDigit("0") },
                tag = "key_0",
                modifier = Modifier.weight(1f)
            )
            TeclaKey(
                text = ",",
                kind = KeyKind.DIGIT,
                onClick = onDot,
                tag = "key_dot",
                modifier = Modifier.weight(1f)
            )
            TeclaKey(
                text = "=",
                kind = KeyKind.EQUALS,
                onClick = onEquals,
                tag = "key_eq",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TeclaKey(
    text: String,
    kind: KeyKind,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    val colors = TeclaTheme.colors
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Determine colors
    val (bgColor, textColor, edgeColor) = when (kind) {
        KeyKind.DIGIT -> Triple(colors.key, colors.keyText, colors.keyEdge)
        KeyKind.OPERATOR -> Triple(colors.accent, colors.accentInk, colors.accent.copy(alpha = 0.7f))
        KeyKind.EQUALS -> Triple(colors.accent, colors.accentInk, colors.accent.copy(alpha = 0.6f))
        KeyKind.FUNCTION -> Triple(colors.fn, colors.text, colors.fnEdge)
        KeyKind.DANGER -> Triple(colors.fn, colors.danger, colors.fnEdge)
        KeyKind.SCIENTIFIC -> Triple(colors.fn, colors.text, colors.fnEdge)
    }

    val fontSize = when (kind) {
        KeyKind.SCIENTIFIC -> 14.sp
        KeyKind.OPERATOR -> 22.sp
        KeyKind.EQUALS -> 24.sp
        KeyKind.DANGER, KeyKind.FUNCTION -> 20.sp
        KeyKind.DIGIT -> 22.sp
    }

    val fontWeight = when (kind) {
        KeyKind.EQUALS -> FontWeight.Bold
        KeyKind.OPERATOR -> FontWeight.SemiBold
        KeyKind.DANGER -> FontWeight.Bold
        KeyKind.FUNCTION -> FontWeight.SemiBold
        else -> FontWeight.Medium
    }

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .heightIn(min = 54.dp)
            .offset(y = if (isPressed) 2.dp else 0.dp)
            .then(
                if (kind == KeyKind.EQUALS) {
                    Modifier.shadow(
                        elevation = if (isPressed) 2.dp else 6.dp,
                        shape = shape,
                        ambientColor = colors.accent.copy(alpha = 0.4f),
                        spotColor = colors.accent.copy(alpha = 0.4f)
                    )
                } else {
                    Modifier.shadow(
                        elevation = if (isPressed) 1.dp else 2.dp,
                        shape = shape
                    )
                }
            )
            .clip(shape)
            .background(bgColor)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) colors.accent else edgeColor,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(bounded = true),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .padding(vertical = 12.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    }
}

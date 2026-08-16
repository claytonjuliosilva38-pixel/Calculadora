package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TeclaTheme

@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier
) {
    val colors = TeclaTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_anim")

    val pulseA by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_a"
    )

    val pulseB by infiniteTransition.animateFloat(
        initialValue = 1.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_b"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-left ambient glow
            val radiusA = width * 0.55f * pulseA
            val centerA = Offset(width * 0.1f, height * 0.1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.accent.copy(alpha = if (colors.isDark) 0.16f else 0.12f),
                        Color.Transparent
                    ),
                    center = centerA,
                    radius = radiusA
                ),
                center = centerA,
                radius = radiusA
            )

            // Bottom-right ambient glow
            val radiusB = width * 0.5f * pulseB
            val centerB = Offset(width * 0.9f, height * 0.85f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.accent.copy(alpha = if (colors.isDark) 0.12f else 0.08f),
                        Color.Transparent
                    ),
                    center = centerB,
                    radius = radiusB
                ),
                center = centerB,
                radius = radiusB
            )
        }
    }
}

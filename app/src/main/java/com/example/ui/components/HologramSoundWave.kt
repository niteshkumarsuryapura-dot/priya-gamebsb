package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.MayaState
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.RedAlert
import kotlin.math.sin

@Composable
fun HologramSoundWave(
    state: MayaState,
    amplitude: Float,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp
) {
    val transition = rememberInfiniteTransition(label = "wave_anim")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val primaryColor = when (state) {
        MayaState.LISTENING -> CyanPrimary
        MayaState.SPEAKING -> AmberAccent
        MayaState.PROCESSING -> BlueAccent
        MayaState.ERROR -> RedAlert
        MayaState.IDLE -> CyanPrimary.copy(alpha = 0.5f)
    }

    val glowColor = when (state) {
        MayaState.LISTENING -> CyanGlow
        MayaState.SPEAKING -> AmberAccent.copy(alpha = 0.3f)
        MayaState.PROCESSING -> BlueAccent.copy(alpha = 0.3f)
        MayaState.ERROR -> RedAlert.copy(alpha = 0.3f)
        MayaState.IDLE -> CyanGlow.copy(alpha = 0.2f)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val totalBars = 32
        val availableWidth = size.width
        val barWidth = (availableWidth / (totalBars * 1.6f)).coerceIn(3f, 10f)
        val spacing = (availableWidth - (totalBars * barWidth)) / (totalBars - 1)
        val centerY = size.height / 2f
        val maxHalfHeight = size.height * 0.45f

        for (i in 0 until totalBars) {
            val normalizedX = i.toFloat() / totalBars.toFloat()
            // Bell curve window to taper edges
            val bellWindow = sin(normalizedX * Math.PI.toFloat())

            // Harmonic wave formula
            val wave1 = sin(phase + i * 0.35f)
            val wave2 = sin(phase * 1.7f + i * 0.55f)
            val combinedWave = (wave1 * 0.6f + wave2 * 0.4f)

            val dynamicFactor = when (state) {
                MayaState.LISTENING -> (amplitude * 0.9f + 0.15f)
                MayaState.SPEAKING -> (amplitude * 0.85f + 0.2f)
                MayaState.PROCESSING -> (sin(phase * 2f + i * 0.4f) * 0.4f + 0.5f)
                MayaState.ERROR -> 0.1f
                MayaState.IDLE -> 0.08f
            }

            val barHeight = ((combinedWave * 0.5f + 0.5f) * maxHalfHeight * dynamicFactor * bellWindow + 3f).coerceAtLeast(3f)

            val xPos = i * (barWidth + spacing)
            val topY = centerY - barHeight
            val bottomY = centerY + barHeight

            // Draw Equalizer Bar
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(glowColor, primaryColor, glowColor),
                    startY = topY,
                    endY = bottomY
                ),
                topLeft = Offset(xPos, topY),
                size = Size(barWidth, barHeight * 2),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

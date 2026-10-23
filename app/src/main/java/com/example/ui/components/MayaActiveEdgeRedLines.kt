package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MayaState

val CyberRed = Color(0xFFFF1744)
val NeonRedBright = Color(0xFFFF0055)
val GlowRedDim = Color(0x66FF1744)

@Composable
fun MayaActiveEdgeRedLines(
    mayaState: MayaState,
    audioAmplitude: Float,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val isActive = isListening || mayaState != MayaState.IDLE

    val infiniteTransition = rememberInfiniteTransition(label = "red_lines_anim")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val cornerBracketLength = 60f
    val cornerStrokeWidth = 4.5f

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("maya_active_edge_red_lines")
    ) {
        // Red Lines along screen corners & full screen edges
        AnimatedVisibility(
            visible = isActive,
            enter = fadeIn(animationSpec = tween(250)),
            exit = fadeOut(animationSpec = tween(350)),
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val padding = 6f
                val dynamicAlpha = (pulseAlpha * (0.8f + (audioAmplitude * 0.2f))).coerceIn(0.5f, 1f)
                val activeRed = CyberRed.copy(alpha = dynamicAlpha)
                val glowRed = NeonRedBright.copy(alpha = dynamicAlpha * 0.45f)

                // 1. Continuous Neon Red Laser Border along entire screen perimeter
                drawRect(
                    color = glowRed,
                    topLeft = Offset(padding - 2f, padding - 2f),
                    size = androidx.compose.ui.geometry.Size(width - (padding * 2) + 4f, height - (padding * 2) + 4f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f)
                )
                drawRect(
                    color = activeRed,
                    topLeft = Offset(padding, padding),
                    size = androidx.compose.ui.geometry.Size(width - (padding * 2), height - (padding * 2)),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                )

                // 2. High-Tech Corner Brackets in all 4 corners (Thick & Glowing Red Lines)
                // Top-Left Corner
                drawLine(
                    color = activeRed,
                    start = Offset(padding, padding),
                    end = Offset(padding + cornerBracketLength, padding),
                    strokeWidth = cornerStrokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = activeRed,
                    start = Offset(padding, padding),
                    end = Offset(padding, padding + cornerBracketLength),
                    strokeWidth = cornerStrokeWidth,
                    cap = StrokeCap.Round
                )

                // Top-Right Corner
                drawLine(
                    color = activeRed,
                    start = Offset(width - padding - cornerBracketLength, padding),
                    end = Offset(width - padding, padding),
                    strokeWidth = cornerStrokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = activeRed,
                    start = Offset(width - padding, padding),
                    end = Offset(width - padding, padding + cornerBracketLength),
                    strokeWidth = cornerStrokeWidth,
                    cap = StrokeCap.Round
                )

                // Bottom-Left Corner
                drawLine(
                    color = activeRed,
                    start = Offset(padding, height - padding - cornerBracketLength),
                    end = Offset(padding, height - padding),
                    strokeWidth = cornerStrokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = activeRed,
                    start = Offset(padding, height - padding),
                    end = Offset(padding + cornerBracketLength, height - padding),
                    strokeWidth = cornerStrokeWidth,
                    cap = StrokeCap.Round
                )

                // Bottom-Right Corner
                drawLine(
                    color = activeRed,
                    start = Offset(width - padding - cornerBracketLength, height - padding),
                    end = Offset(width - padding, height - padding),
                    strokeWidth = cornerStrokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = activeRed,
                    start = Offset(width - padding, height - padding - cornerBracketLength),
                    end = Offset(width - padding, height - padding),
                    strokeWidth = cornerStrokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        // 3. Corner Print / HUD Indicator ("Yah line aur yah print hamesha activate hona")
        AnimatedVisibility(
            visible = isActive,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 38.dp, end = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E0A10).copy(alpha = 0.9f))
                    .border(1.2.dp, CyberRed.copy(alpha = pulseAlpha), RoundedCornerShape(12.dp))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
                    .testTag("red_line_active_print")
            ) {
                // Pulsing Red Dot
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(CyberRed.copy(alpha = pulseAlpha))
                )

                val stateText = when (mayaState) {
                    MayaState.LISTENING -> "MAYA ACTIVE • LISTENING"
                    MayaState.PROCESSING -> "MAYA ACTIVE • ANALYZING"
                    MayaState.SPEAKING -> "MAYA ACTIVE • SPEAKING"
                    MayaState.ERROR -> "MAYA ACTIVE • STANDBY"
                    MayaState.IDLE -> "MAYA ACTIVE 🔴"
                }

                Text(
                    text = stateText,
                    color = CyberRed,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

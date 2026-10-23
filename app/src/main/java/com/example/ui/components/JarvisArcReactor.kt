package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MayaState
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.RedAlert
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisArcReactor(
    state: MayaState,
    amplitude: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_reactor")

    // Continuous smooth rotations
    val rotationFast by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    MayaState.PROCESSING -> 1600
                    MayaState.LISTENING -> 4000
                    MayaState.SPEAKING -> 2800
                    else -> 8000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_fast"
    )

    val rotationReverse by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    MayaState.PROCESSING -> 2200
                    MayaState.LISTENING -> 5000
                    MayaState.SPEAKING -> 3500
                    else -> 10000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_reverse"
    )

    // Breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_pulse"
    )

    // Dynamic state colors
    val corePrimaryColor = when (state) {
        MayaState.LISTENING -> CyanPrimary
        MayaState.SPEAKING -> AmberAccent
        MayaState.PROCESSING -> BlueAccent
        MayaState.ERROR -> RedAlert
        MayaState.IDLE -> CyanPrimary
    }

    val coreGlowColor = when (state) {
        MayaState.LISTENING -> CyanGlow
        MayaState.SPEAKING -> AmberAccent.copy(alpha = 0.45f)
        MayaState.PROCESSING -> BlueAccent.copy(alpha = 0.45f)
        MayaState.ERROR -> RedAlert.copy(alpha = 0.45f)
        MayaState.IDLE -> CyanGlow
    }

    val avatarScale = when (state) {
        MayaState.SPEAKING -> (1f + amplitude * 0.18f).coerceIn(0.98f, 1.18f)
        MayaState.LISTENING -> (1f + amplitude * 0.12f).coerceIn(0.98f, 1.14f)
        MayaState.PROCESSING -> breathingPulse
        else -> breathingPulse
    }

    val avatarDiameter = size * 0.46f

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2, color = corePrimaryColor),
                onClick = onClick
            )
            .testTag("jarvis_arc_reactor"),
        contentAlignment = Alignment.Center
    ) {
        // Holographic Cyber Rings Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f * 0.88f

            // 1. Outermost audio-reactive expansion aura
            val reactiveExpansion = amplitude * (baseRadius * 0.22f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(coreGlowColor, Color.Transparent),
                    center = center,
                    radius = baseRadius + reactiveExpansion
                ),
                radius = baseRadius + reactiveExpansion,
                center = center
            )

            // 2. Outer segmented tech ring
            rotate(rotationFast, pivot = center) {
                drawOuterSegmentedRing(center, baseRadius * 0.94f, corePrimaryColor)
            }

            // 3. Counter-rotating HUD tick ring
            rotate(rotationReverse, pivot = center) {
                drawTickRing(center, baseRadius * 0.80f, corePrimaryColor.copy(alpha = 0.6f))
                drawCrosshairArcs(center, baseRadius * 0.80f, corePrimaryColor)
            }

            // 4. Middle Energy Circuit Ring with dynamic amplitude pulses
            val pulseRadius = (baseRadius * 0.62f) * (if (state == MayaState.IDLE) breathingPulse else (1f + amplitude * 0.15f))
            drawCircle(
                color = corePrimaryColor.copy(alpha = 0.4f),
                radius = pulseRadius,
                center = center,
                style = Stroke(
                    width = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), rotationFast * 0.5f)
                )
            )

            // 5. Orbiting Quantum Energy Particles
            rotate(rotationFast * 1.5f, pivot = center) {
                drawOrbitingParticles(center, baseRadius * 0.72f, corePrimaryColor)
            }
        }

        // Center Maya Girl Avatar
        Box(
            modifier = Modifier
                .size(avatarDiameter)
                .scale(avatarScale)
                .clip(CircleShape)
                .background(DarkBackground)
                .border(2.5.dp, corePrimaryColor, CircleShape)
                .border(6.dp, corePrimaryColor.copy(alpha = 0.25f), CircleShape)
                .testTag("maya_avatar_image"),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.maya_avatar),
                contentDescription = "Maya AI Assistant Face",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Subtle glowing cyber tint when speaking or listening
            if (state == MayaState.SPEAKING || state == MayaState.LISTENING) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(corePrimaryColor.copy(alpha = 0.15f * (0.4f + amplitude * 0.6f)))
                )
            }
        }
    }
}

private fun DrawScope.drawOuterSegmentedRing(center: Offset, radius: Float, color: Color) {
    val segments = 12
    val sweepAngle = 20f
    val gapAngle = 10f

    for (i in 0 until segments) {
        val startAngle = i * (sweepAngle + gapAngle)
        drawArc(
            color = color.copy(alpha = 0.75f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawTickRing(center: Offset, radius: Float, color: Color) {
    val totalTicks = 36
    for (i in 0 until totalTicks) {
        val angleRad = (i * (360f / totalTicks)) * (PI.toFloat() / 180f)
        val tickLength = if (i % 6 == 0) 12f else 6f
        val startX = center.x + (radius - tickLength) * cos(angleRad)
        val startY = center.y + (radius - tickLength) * sin(angleRad)
        val endX = center.x + radius * cos(angleRad)
        val endY = center.y + radius * sin(angleRad)

        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (i % 6 == 0) 2.5f else 1.2f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawCrosshairArcs(center: Offset, radius: Float, color: Color) {
    val arcLen = 45f
    val angles = floatArrayOf(0f, 90f, 180f, 270f)
    for (ang in angles) {
        drawArc(
            color = color,
            startAngle = ang - arcLen / 2,
            sweepAngle = arcLen,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = 2.5f)
        )
    }
}

private fun DrawScope.drawMayaEmblem(center: Offset, size: Float, state: MayaState) {
    // Holographic 'M' futuristic vector shape in center
    val half = size * 0.6f
    val color = when (state) {
        MayaState.LISTENING -> Color.White
        MayaState.SPEAKING -> Color(0xFF1E1100)
        else -> Color(0xFF030D1B)
    }

    // Outer cyber hexagon / diamond
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x - half, center.y + half * 0.7f)
        lineTo(center.x - half, center.y - half * 0.7f)
        lineTo(center.x, center.y + half * 0.1f)
        lineTo(center.x + half, center.y - half * 0.7f)
        lineTo(center.x + half, center.y + half * 0.7f)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 4.5f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawOrbitingParticles(center: Offset, radius: Float, color: Color) {
    val count = 4
    for (i in 0 until count) {
        val angleRad = (i * (360f / count)) * (PI.toFloat() / 180f)
        val px = center.x + radius * cos(angleRad)
        val py = center.y + radius * sin(angleRad)

        drawCircle(
            color = color,
            radius = 4.5f,
            center = Offset(px, py)
        )
        drawCircle(
            color = Color.White,
            radius = 2f,
            center = Offset(px, py)
        )
    }
}

package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.HearingDisabled
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import com.example.data.model.PriyaEmotion
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MayaState
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedAlert
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TelemetryHeader(
    state: MayaState,
    memoryCount: Int,
    isWakeWordActive: Boolean,
    onToggleWakeWord: () -> Unit,
    onOpenPopo: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenMemories: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusText = when (state) {
        MayaState.IDLE -> if (isWakeWordActive) "ALWAYS LISTENING • 'HEY MAYA'" else "CORE ONLINE • STANDBY"
        MayaState.LISTENING -> "AUDIO CAPTURE • HINDI/ENG"
        MayaState.PROCESSING -> "NEURAL SYNTHESIS • GEMINI"
        MayaState.SPEAKING -> "VOICE CORE • HINDI ACTIVE"
        MayaState.ERROR -> "SYSTEM ALERT"
    }

    val statusColor = when (state) {
        MayaState.IDLE -> if (isWakeWordActive) CyanPrimary else GreenSuccess
        MayaState.LISTENING -> CyanPrimary
        MayaState.PROCESSING -> BlueAccent
        MayaState.SPEAKING -> AmberAccent
        MayaState.ERROR -> RedAlert
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Top HUD line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Maya Brand Info with Miniature Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(GlassSurfaceElevated)
                        .border(1.5.dp, CyanPrimary, CircleShape)
                        .clickable(onClick = onOpenPopo)
                        .testTag("maya_header_avatar"),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.maya_avatar),
                        contentDescription = "Maya Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PRIYA",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "AI v4.0",
                            color = CyanPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(
                                    CyanPrimary.copy(alpha = 0.15f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = "JARVIS Voice & Vision • Hindi/Eng",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            // Action Quick Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // POPO Telegram Channel button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassSurfaceElevated)
                        .border(1.dp, CyanPrimary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenPopo)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("open_popo_channel_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "POPO",
                            tint = CyanPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "POPO",
                            color = CyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // App Permissions Dialog
                IconButton(
                    onClick = onOpenPermissions,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, CircleShape)
                        .testTag("open_permissions_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Permissions",
                        tint = AmberAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Memories / Knowledge Bank
                IconButton(
                    onClick = onOpenMemories,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, CircleShape)
                        .testTag("open_memories_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Memory Bank",
                        tint = if (memoryCount > 0) AmberAccent else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // History
                IconButton(
                    onClick = onOpenHistory,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, CircleShape)
                        .testTag("open_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Conversation History",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Settings
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, CircleShape)
                        .testTag("open_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Frosted Glass Status Telemetry Bar with Wake Word Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(GlassSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Pulsing status LED
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 10.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp,
                    maxLines = 1
                )
            }

            // Wake Word toggle chip
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isWakeWordActive) CyanPrimary.copy(alpha = 0.2f) else GlassSurfaceElevated)
                    .border(1.dp, if (isWakeWordActive) CyanPrimary else GlassBorder, RoundedCornerShape(10.dp))
                    .clickable(onClick = onToggleWakeWord)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                    .testTag("toggle_wake_word_chip"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isWakeWordActive) Icons.Default.Hearing else Icons.Default.HearingDisabled,
                    contentDescription = "Wake Word",
                    tint = if (isWakeWordActive) CyanPrimary else TextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isWakeWordActive) "WAKE: ON" else "WAKE: OFF",
                    color = if (isWakeWordActive) CyanPrimary else TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

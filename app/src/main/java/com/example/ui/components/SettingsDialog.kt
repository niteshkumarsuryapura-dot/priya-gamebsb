package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.HologramBorder
import com.example.ui.theme.RedAlert
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AppLauncherManager
import com.example.util.AppUpdateManager
import kotlin.math.roundToInt

@Composable
fun SettingsDialog(
    voicePitch: Float,
    speechRate: Float,
    hasApiKey: Boolean,
    updateManager: AppUpdateManager,
    isCheckingUpdate: Boolean,
    onPitchChange: (Float) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onTestVoice: () -> Unit,
    onCheckUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val launcherManager = remember { AppLauncherManager(context) }
    var simulateV2 by remember { mutableStateOf(updateManager.isSimulatedUpdateEnabled()) }
    var telegramInput by remember { mutableStateOf(launcherManager.getRawTelegramChannel().ifBlank { AppLauncherManager.POPO_TELEGRAM_URL }) }
    var channelSavedNotice by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, GlassBorderCyan, RoundedCornerShape(24.dp)),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GlassSurfaceElevated)
                                .border(1.dp, GlassBorderCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MAYA SYSTEM SETTINGS",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Voice tuning & AI Core configurations",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Voice Pitch Setting
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassSurfaceElevated)
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Female Voice Pitch (स्वर स्तर)",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(voicePitch * 100).roundToInt() / 100f}x",
                            color = CyanPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = voicePitch,
                        onValueChange = onPitchChange,
                        valueRange = 0.8f..1.4f,
                        modifier = Modifier.testTag("pitch_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = CyanPrimary,
                            activeTrackColor = CyanPrimary,
                            inactiveTrackColor = GlassBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Speech Speed Rate
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassSurfaceElevated)
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Speech Speed Rate (बोलने की गति)",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(speechRate * 100).roundToInt() / 100f}x ${if (speechRate >= 1.25f) "⚡" else ""}",
                            color = AmberAccent,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = speechRate,
                        onValueChange = onSpeechRateChange,
                        valueRange = 0.8f..2.0f,
                        modifier = Modifier.testTag("speed_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = AmberAccent,
                            activeTrackColor = AmberAccent,
                            inactiveTrackColor = GlassBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Preset quick buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("1.0x", 1.0f, "सामान्य"),
                            Triple("1.25x ⚡", 1.25f, "तेज़"),
                            Triple("1.5x 🚀", 1.50f, "सुपर तेज़")
                        ).forEach { (label, rate, desc) ->
                            val isSelected = (speechRate * 100).roundToInt() == (rate * 100).roundToInt()
                            Button(
                                onClick = { onSpeechRateChange(rate) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("speed_preset_${rate}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) AmberAccent else GlassSurface,
                                    contentColor = if (isSelected) DarkBackground else TextSecondary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(text = desc, fontSize = 8.5.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "💡 बोलकर भी कह सकते हैं: \"आवाज़ तेज़ करो\" या \"तेज़ बोलो\"",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Test voice button
                Button(
                    onClick = onTestVoice,
                    modifier = Modifier.fillMaxWidth().testTag("test_voice_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlassSurfaceElevated,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Maya Voice (आवाज़ जांचें)", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // API Key & Model Status Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = if (hasApiKey) GreenSuccess else AmberAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (hasApiKey) "Gemini API Engine Active" else "API Key: Configured via Secrets",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Model: gemini-2.5-flash • High Knowledge Base",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // App Version & Automatic Telegram Update Center
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassSurfaceElevated)
                        .border(1.dp, GlassBorderCyan, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Maya AI Version: ${AppUpdateManager.CURRENT_VERSION_NAME}",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Telegram Auto-Update System",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Button(
                            onClick = onCheckUpdate,
                            modifier = Modifier.testTag("check_update_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPrimary.copy(alpha = 0.2f),
                                contentColor = CyanPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            if (isCheckingUpdate) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = CyanPrimary,
                                    strokeWidth = 1.5.dp
                                )
                            } else {
                                Text("Check (जांचें)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Version 2 Testing / Simulation Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(GlassSurface)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Simulate New Version 2.0 (अपडेट टेस्ट)",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "नया अपडेट आने पर कैसा दिखेगा, इसे टेस्ट करें",
                                color = TextMuted,
                                fontSize = 9.5.sp
                            )
                        }

                        Switch(
                            checked = simulateV2,
                            onCheckedChange = { isChecked ->
                                simulateV2 = isChecked
                                updateManager.setSimulateUpdate(isChecked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyanPrimary,
                                checkedTrackColor = CyanPrimary.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = GlassBorder
                            ),
                            modifier = Modifier.testTag("simulate_v2_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Direct Telegram Channel ID / Link editor
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(GlassSurface)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Direct Telegram Channel ID / Link:",
                            color = CyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = telegramInput,
                            onValueChange = {
                                telegramInput = it
                                channelSavedNotice = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("telegram_channel_input"),
                            placeholder = {
                                Text(
                                    text = "@channel_name या link",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = GlassBorder,
                                cursorColor = CyanPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (channelSavedNotice) {
                                Text(
                                    text = "✓ Channel Saved!",
                                    color = GreenSuccess,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "अपडेट डाउनलोड व कम्युनिटी लिंक",
                                    color = TextMuted,
                                    fontSize = 9.5.sp
                                )
                            }

                            Button(
                                onClick = {
                                    launcherManager.setTelegramChannel(telegramInput)
                                    channelSavedNotice = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanPrimary,
                                    contentColor = DarkBackground
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("save_channel_id_button")
                            ) {
                                Text("Save (सेव करें)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}


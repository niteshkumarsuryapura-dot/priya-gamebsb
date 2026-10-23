package com.example.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.media.projection.MediaProjectionManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.StopScreenShare
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MayaState
import com.example.service.MayaBackgroundService
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.ConversationSheet
import com.example.ui.components.HologramSoundWave
import com.example.ui.components.JarvisArcReactor
import com.example.ui.components.MayaActiveEdgeRedLines
import com.example.ui.components.MemorySheet
import com.example.ui.components.PermissionsDialog
import com.example.ui.components.PopoTelegramDialog
import com.example.ui.components.QuickActionPanel
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TelemetryHeader
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceCyan
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.HologramBorder
import com.example.ui.theme.RedAlert
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MayaMainScreen(
    viewModel: MayaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val mayaState by viewModel.mayaState.collectAsStateWithLifecycle()
    val lastResponse by viewModel.lastResponse.collectAsStateWithLifecycle()
    val liveTranscript by viewModel.liveTranscript.collectAsStateWithLifecycle()
    val amplitude by viewModel.audioAmplitude.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()
    val showHistorySheet by viewModel.showHistorySheet.collectAsStateWithLifecycle()
    val showMemorySheet by viewModel.showMemorySheet.collectAsStateWithLifecycle()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
    val showPopoDialog by viewModel.showPopoDialog.collectAsStateWithLifecycle()
    val showPermissionsDialog by viewModel.showPermissionsDialog.collectAsStateWithLifecycle()
    val isWakeWordActive by viewModel.isWakeWordActive.collectAsStateWithLifecycle()
    val pendingUpdate by viewModel.pendingUpdate.collectAsStateWithLifecycle()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsStateWithLifecycle()
    val voicePitch by viewModel.voicePitch.collectAsStateWithLifecycle()
    val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()
    val voiceError by viewModel.voiceErrorMessage.collectAsStateWithLifecycle()

    val isScreenSharing by viewModel.isScreenSharing.collectAsStateWithLifecycle()
    val capturedPhoto by viewModel.capturedPhoto.collectAsStateWithLifecycle()
    val isAnalyzingVisual by viewModel.isAnalyzingVisual.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    var showTextInputBar by remember { mutableStateOf(false) }

    // Audio Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            MayaBackgroundService.start(context)
            viewModel.onMicClicked()
        } else {
            Toast.makeText(
                context,
                "Microphone permission is required for voice assistant",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Screen Capture Permission Launcher
    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.onScreenCapturePermissionGranted(result.resultCode, result.data!!)
        } else {
            viewModel.speakText("स्क्रीन शेयरिंग अनुमति रद्द कर दी गई।")
        }
    }

    // Camera Capture Launcher (returns direct bitmap)
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.onPhotoCaptured(bitmap, "Explain what is in this captured photo")
        } else {
            viewModel.speakText("फ़ोटो नहीं ली गई।")
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePhotoLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
            viewModel.speakText("फ़ोटो लेने के लिए कैमरा परमिशन की आवश्यकता है।")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onRequestScreenCapturePermission = {
            val mpManager = context.getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
            if (mpManager != null) {
                screenCaptureLauncher.launch(mpManager.createScreenCaptureIntent())
            }
        }
        viewModel.onRequestCameraCapture = {
            val hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (hasCamera) {
                takePhotoLauncher.launch(null)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Google System Speech Input Launcher (guaranteed to work across all devices/emulators)
    val systemSpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenMatches?.firstOrNull { it.isNotBlank() }
            if (!spokenText.isNullOrBlank()) {
                viewModel.processUserPrompt(spokenText.trim(), isVoice = true)
            }
        }
    }

    val launchSystemVoiceInput: () -> Unit = {
        val hasAudioPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAudioPerm) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            try {
                val intent = viewModel.voiceManager.createSpeechRecognizerIntent()
                systemSpeechLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Voice input dialog: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val handleMicClick = {
        val hasAudioPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAudioPerm) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            if (mayaState == MayaState.SPEAKING) {
                viewModel.stopAllSpeech()
            } else if (mayaState == MayaState.LISTENING) {
                viewModel.voiceManager.stopListening()
            } else {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    viewModel.onMicClicked()
                } else {
                    launchSystemVoiceInput()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CyanPrimary.copy(alpha = 0.08f),
                            Color(0xFF0A1420),
                            DarkBackground
                        ),
                        radius = 1400f
                    )
                )
        ) {
            // High-Tech Red Corner and Edge Lines overlay when Maya is active
            MayaActiveEdgeRedLines(
                mayaState = mayaState,
                audioAmplitude = amplitude,
                isListening = (mayaState == MayaState.LISTENING),
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // 1. HUD Telemetry Header
                TelemetryHeader(
                    state = mayaState,
                    memoryCount = memories.size,
                    isWakeWordActive = isWakeWordActive,
                    onToggleWakeWord = { viewModel.toggleWakeWordMode() },
                    onOpenPopo = { viewModel.setShowPopoDialog(true) },
                    onOpenPermissions = { viewModel.setShowPermissionsDialog(true) },
                    onOpenMemories = { viewModel.setShowMemorySheet(true) },
                    onOpenHistory = { viewModel.setShowHistorySheet(true) },
                    onOpenSettings = { viewModel.setShowSettingsDialog(true) }
                )

                // 2. Main Scrollable Core Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Holographic JARVIS Arc Reactor Centerpiece
                    JarvisArcReactor(
                        state = mayaState,
                        amplitude = amplitude,
                        onClick = handleMicClick,
                        size = 230.dp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Audio Frequency Hologram Waves
                    HologramSoundWave(
                        state = mayaState,
                        amplitude = amplitude,
                        height = 36.dp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Vision, Screen Sharing & Photo Capture Control Panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        if (isScreenSharing) {
                            // Active Screen Share Status & Control Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(GlassSurfaceElevated)
                                    .border(1.5.dp, GreenSuccess, RoundedCornerShape(18.dp))
                                    .padding(12.dp)
                                    .testTag("active_screen_share_card")
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(GreenSuccess)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ScreenShare,
                                                contentDescription = "Screen Share",
                                                tint = GreenSuccess,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "SCREEN SHARING ACTIVE",
                                                color = GreenSuccess,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        // Stop Button
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(RedAlert.copy(alpha = 0.2f))
                                                .border(1.dp, RedAlert, RoundedCornerShape(12.dp))
                                                .clickable { viewModel.stopScreenSharing() }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                                .testTag("stop_screen_share_button")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.StopScreenShare,
                                                    contentDescription = "Stop Sharing",
                                                    tint = RedAlert,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "STOP SHARING",
                                                    color = RedAlert,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Explain Screen Action Button
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(CyanPrimary)
                                            .clickable { viewModel.explainCurrentScreen() }
                                            .padding(vertical = 10.dp)
                                            .testTag("explain_screen_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Visibility,
                                                contentDescription = "Explain Screen",
                                                tint = DarkBackground,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "EXPLAIN CURRENT SCREEN (स्क्रीन समझाओ)",
                                                color = DarkBackground,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Quick Action Row: Screen Share & Take Photo buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Share Screen Button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(GlassSurfaceElevated)
                                        .border(1.dp, CyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                        .clickable { viewModel.requestScreenSharing() }
                                        .padding(vertical = 10.dp, horizontal = 12.dp)
                                        .testTag("request_screen_share_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ScreenShare,
                                            contentDescription = "Share Screen",
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "SHARE SCREEN",
                                            color = CyanPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                // Take Photo Button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(GlassSurfaceElevated)
                                        .border(1.dp, AmberAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                        .clickable { viewModel.requestPhotoCapture() }
                                        .padding(vertical = 10.dp, horizontal = 12.dp)
                                        .testTag("request_photo_capture_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Take Photo",
                                            tint = AmberAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "TAKE PHOTO",
                                            color = AmberAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        // Captured Photo Preview Card (if photo was taken)
                        capturedPhoto?.let { photoBmp ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(GlassSurfaceElevated)
                                    .border(1.dp, AmberAccent, RoundedCornerShape(18.dp))
                                    .padding(12.dp)
                                    .testTag("captured_photo_preview_card")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        bitmap = photoBmp.asImageBitmap(),
                                        contentDescription = "Captured Photo",
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, AmberAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "PHOTO CAPTURED",
                                            color = AmberAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(AmberAccent)
                                                .clickable {
                                                    viewModel.onPhotoCaptured(photoBmp, "Explain what is in this captured photo")
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "EXPLAIN PHOTO (फ़ोटो समझाओ)",
                                                color = DarkBackground,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { viewModel.clearCapturedPhoto() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close Preview",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Frosted Glass Live Response / Transcript Terminal Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(GlassSurfaceElevated)
                            .border(1.dp, GlassBorderCyan, RoundedCornerShape(22.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = when (mayaState) {
                                            MayaState.LISTENING -> CyanPrimary
                                            MayaState.SPEAKING -> AmberAccent
                                            MayaState.PROCESSING -> BlueAccent
                                            else -> CyanPrimary
                                        },
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (mayaState) {
                                            MayaState.LISTENING -> "LIVE TRANSCRIPT (सुन रही हूँ...)"
                                            MayaState.PROCESSING -> if (isAnalyzingVisual) "ANALYZING VISUAL (स्क्रीन/फ़ोटो देख रही हूँ...)" else "THINKING (सोच रही हूँ...)"
                                            MayaState.SPEAKING -> "PRIYA SPEAKING (बोल रही हूँ...)"
                                            else -> "PRIYA INTELLIGENCE (प्रिया AI)"
                                        },
                                        color = CyanPrimary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GlassSurface)
                                            .border(1.dp, if (speechRate >= 1.2f) AmberAccent else GlassBorder, RoundedCornerShape(8.dp))
                                            .clickable {
                                                val nextRate = when {
                                                    speechRate < 1.15f -> 1.25f
                                                    speechRate < 1.40f -> 1.50f
                                                    else -> 1.0f
                                                }
                                                viewModel.setSpeechRate(nextRate)
                                                Toast.makeText(context, "बोलने की गति: ${String.format(java.util.Locale.US, "%.2f", nextRate)}x", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(horizontal = 7.dp, vertical = 4.dp)
                                            .testTag("quick_speed_toggle")
                                    ) {
                                        Text(
                                            text = "${(speechRate * 100).roundToInt() / 100f}x ⚡",
                                            color = if (speechRate >= 1.2f) AmberAccent else TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    if (mayaState == MayaState.SPEAKING) {
                                        IconButton(
                                            onClick = { viewModel.stopAllSpeech() },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(RedAlert.copy(alpha = 0.15f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Stop,
                                                contentDescription = "Stop",
                                                tint = RedAlert,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else {
                                        IconButton(
                                            onClick = { viewModel.speakText(lastResponse) },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(GlassSurface)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = "Replay",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val displayText = when {
                                mayaState == MayaState.LISTENING -> {
                                    if (liveTranscript.isNotBlank()) "🎙️ $liveTranscript" else "🎙️ सुन रही हूँ... कृपया बोलिए (Speak now...)"
                                }
                                mayaState == MayaState.PROCESSING -> "⚡ विश्लेषण और उत्तर तैयार किया जा रहा है..."
                                voiceError != null -> "⚠️ $voiceError\n\n💡 सुझाव: दाएँ तरफ 'GOOGLE MIC' दबाएँ या 'TYPE QUERY' से लिखकर पूछें।"
                                else -> lastResponse
                            }

                            Text(
                                text = displayText,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Smart Knowledge Prompts
                    QuickActionPanel(
                        onPromptSelected = { prompt ->
                            viewModel.processUserPrompt(prompt, isVoice = true)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 3. Bottom Controls Area (Frosted Glass Control Bar)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Optional Text Input Row
                    AnimatedVisibility(
                        visible = showTextInputBar,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = { Text("माया से पूछें... (Type query to Maya)", fontSize = 13.sp, color = TextMuted) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("text_query_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = GlassSurfaceElevated,
                                    unfocusedContainerColor = GlassSurface
                                ),
                                shape = RoundedCornerShape(24.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (textInput.isNotBlank()) {
                                        viewModel.processUserPrompt(textInput.trim(), isVoice = false)
                                        textInput = ""
                                        focusManager.clearFocus()
                                    }
                                })
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (textInput.isNotBlank()) {
                                        viewModel.processUserPrompt(textInput.trim(), isVoice = false)
                                        textInput = ""
                                        focusManager.clearFocus()
                                    }
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary)
                                    .testTag("send_query_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = DarkBackground,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Main Push-To-Talk Mic & Toggle Bar in Frosted Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(32.dp))
                            .background(GlassSurface)
                            .border(1.dp, GlassBorder, RoundedCornerShape(32.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Keyboard / Text mode toggle
                            Text(
                                text = if (showTextInputBar) "VOICE MODE" else "TYPE QUERY",
                                color = CyanPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(GlassSurfaceElevated)
                                    .border(1.dp, GlassBorderCyan, RoundedCornerShape(16.dp))
                                    .clickable { showTextInputBar = !showTextInputBar }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("toggle_input_mode_button")
                            )

                            // Central Glowing Voice Button
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (mayaState) {
                                            MayaState.LISTENING -> RedAlert
                                            MayaState.SPEAKING -> AmberAccent
                                            MayaState.PROCESSING -> BlueAccent
                                            else -> CyanPrimary
                                        }
                                    )
                                    .clickable(onClick = handleMicClick)
                                    .testTag("main_mic_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (mayaState) {
                                        MayaState.LISTENING -> Icons.Default.Mic
                                        MayaState.SPEAKING -> Icons.Default.Stop
                                        MayaState.PROCESSING -> Icons.Default.GraphicEq
                                        else -> Icons.Default.Mic
                                    },
                                    contentDescription = "Maya Voice Control",
                                    tint = DarkBackground,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            // Stop / Reset or System Voice Input button
                            if (mayaState != MayaState.IDLE) {
                                Text(
                                    text = "INTERRUPT",
                                    color = RedAlert,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(GlassSurfaceElevated)
                                        .border(
                                            1.dp,
                                            RedAlert.copy(alpha = 0.5f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { viewModel.stopAllSpeech() }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("interrupt_button")
                                )
                            } else {
                                Text(
                                    text = "GOOGLE MIC 🎙️",
                                    color = AmberAccent,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(GlassSurfaceElevated)
                                        .border(
                                            1.dp,
                                            AmberAccent.copy(alpha = 0.6f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { launchSystemVoiceInput() }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                        .testTag("google_system_mic_button")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when (mayaState) {
                            MayaState.LISTENING -> "Listening • बोलकर समाप्त करें (Tap to finish)"
                            MayaState.SPEAKING -> "Priya is speaking • आवाज़ रोकने के लिए टैप करें"
                            MayaState.PROCESSING -> "Priya is synthesizing answer • कृपया प्रतीक्षा करें"
                            else -> "Tap Mic, Screen Share or Camera to interact with Priya"
                        },
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Bottom Sheets and Dialogs
        if (showHistorySheet) {
            ConversationSheet(
                messages = messages,
                onDismiss = { viewModel.setShowHistorySheet(false) },
                onSpeakMessage = { text -> viewModel.speakText(text) },
                onClearHistory = { viewModel.clearHistory() }
            )
        }

        if (showMemorySheet) {
            MemorySheet(
                memories = memories,
                onDismiss = { viewModel.setShowMemorySheet(false) },
                onSaveMemory = { key, value, cat -> viewModel.saveMemory(key, value, cat) },
                onDeleteMemory = { id -> viewModel.deleteMemory(id) },
                onClearAllMemories = { viewModel.clearAllMemories() }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                voicePitch = voicePitch,
                speechRate = speechRate,
                hasApiKey = viewModel.hasApiKey,
                updateManager = viewModel.updateManager,
                isCheckingUpdate = isCheckingUpdate,
                onPitchChange = { viewModel.setPitch(it) },
                onSpeechRateChange = { viewModel.setSpeechRate(it) },
                onTestVoice = { viewModel.testVoice() },
                onCheckUpdate = { viewModel.triggerUpdateCheck() },
                onDismiss = { viewModel.setShowSettingsDialog(false) }
            )
        }

        if (showPopoDialog) {
            PopoTelegramDialog(
                onDismiss = { viewModel.setShowPopoDialog(false) }
            )
        }

        if (showPermissionsDialog) {
            PermissionsDialog(
                launcherManager = viewModel.launcherManager,
                onDismiss = { viewModel.setShowPermissionsDialog(false) }
            )
        }

        // Automatic / Mandatory Version Update Dialog
        pendingUpdate?.let { updateInfo ->
            AppUpdateDialog(
                updateInfo = updateInfo,
                onUpdateClick = { viewModel.onUpdateRequested() },
                onExitClick = { viewModel.onRefuseUpdate(context as? Activity) },
                onDismiss = if (!updateInfo.isMandatory) { { viewModel.dismissUpdate() } } else null
            )
        }
    }
}

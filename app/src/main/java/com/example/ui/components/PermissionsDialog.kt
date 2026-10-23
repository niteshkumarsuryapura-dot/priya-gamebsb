package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.core.content.ContextCompat
import com.example.ui.theme.AmberAccent
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
import com.example.util.AppLauncherManager
import com.example.util.AppPermissionItem

@Composable
fun PermissionsDialog(
    launcherManager: AppLauncherManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    var appList by remember { mutableStateOf(launcherManager.getSupportedApps()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, GlassBorderCyan, RoundedCornerShape(24.dp))
                .testTag("permissions_dialog"),
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
                                .border(1.dp, CyanPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Security",
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "APP PERMISSIONS & ACCESS",
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "अनुमतियाँ एवं ऐप नियंत्रण",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_permissions_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Info banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "🔒 Maya will ONLY access and open apps that you enable below (माया केवल उन्हीं ऐप्स को खोलेगी जिनकी आप अनुमति देंगे)।",
                        color = AmberAccent,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "SYSTEM PERMISSIONS (सिस्टम अनुमतियाँ)",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // 1. Microphone Permission
                    item {
                        SystemPermissionRow(
                            title = "Microphone (माइक्रोफ़ोन)",
                            subtitle = "Voice recognition & 'Hey Maya' wake word",
                            isGranted = hasAudioPermission,
                            onRequest = {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        )
                    }

                    // 2. Notifications Permission
                    item {
                        SystemPermissionRow(
                            title = "Notifications (सूचनाएं)",
                            subtitle = "Background assistant status alerts",
                            isGranted = hasNotificationPermission,
                            onRequest = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        )
                    }

                    // 3. Screen Overlay / Floating Window
                    item {
                        SystemPermissionRow(
                            title = "Overlay (स्क्रीन ओवरले)",
                            subtitle = "Show Maya over other apps & home screen",
                            isGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true,
                            onRequest = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ALLOWED APPS ACCESS (ऐप खोलने की अनुमति)",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // App switches
                    items(appList, key = { it.key }) { app ->
                        AppAccessToggleRow(
                            item = app,
                            onToggle = { allowed ->
                                launcherManager.setAppAllowed(app.key, allowed)
                                appList = launcherManager.getSupportedApps()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemPermissionRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GlassSurfaceElevated)
            .border(1.dp, if (isGranted) GlassBorder else AmberAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGranted) GreenSuccess else AmberAccent,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        if (!isGranted) {
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("Allow", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Text(
                text = "ALLOWED",
                color = GreenSuccess,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun AppAccessToggleRow(
    item: AppPermissionItem,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GlassSurfaceElevated)
            .border(1.dp, if (item.isEnabled) GlassBorderCyan else GlassBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(GlassSurface)
                    .border(1.dp, GlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.name,
                    tint = if (item.isEnabled) CyanPrimary else TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = item.name,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.hindiName,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }

        if (item.isAlwaysAllowed) {
            Text(
                text = "ALWAYS ON",
                color = CyanPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        } else {
            Switch(
                checked = item.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DarkBackground,
                    checkedTrackColor = CyanPrimary,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = GlassSurface
                )
            )
        }
    }
}

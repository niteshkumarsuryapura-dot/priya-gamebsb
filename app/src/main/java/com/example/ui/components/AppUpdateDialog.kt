package com.example.ui.components

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.RedAlert
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AppUpdateInfo

@Composable
fun AppUpdateDialog(
    updateInfo: AppUpdateInfo,
    onUpdateClick: () -> Unit,
    onExitClick: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Dialog(
        onDismissRequest = {
            if (updateInfo.isMandatory) {
                onExitClick()
            } else {
                onDismiss?.invoke()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !updateInfo.isMandatory,
            dismissOnClickOutside = !updateInfo.isMandatory
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, if (updateInfo.isMandatory) RedAlert else GlassBorderCyan, RoundedCornerShape(24.dp))
                .testTag("app_update_dialog"),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Update Icon & Badge
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
                                .background(if (updateInfo.isMandatory) RedAlert.copy(alpha = 0.2f) else GlassSurfaceElevated)
                                .border(1.dp, if (updateInfo.isMandatory) RedAlert else CyanPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "Update",
                                tint = if (updateInfo.isMandatory) RedAlert else CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CRITICAL UPDATE",
                                color = if (updateInfo.isMandatory) RedAlert else CyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "नया वर्जन आवश्यक (New Version)",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (!updateInfo.isMandatory && onDismiss != null) {
                        androidx.compose.material3.IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_update_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Avatar Photo
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .border(2.dp, CyanPrimary, CircleShape)
                        .border(6.dp, CyanPrimary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.maya_avatar),
                        contentDescription = "Maya Version 2 Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title & Version comparison
                Text(
                    text = updateInfo.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Current: ${updateInfo.currentVersion}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "➔",
                        color = CyanPrimary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = updateInfo.latestVersion,
                        color = AmberAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mandatory Update Warning Alert
                if (updateInfo.isMandatory) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(RedAlert.copy(alpha = 0.12f))
                            .border(1.dp, RedAlert.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = RedAlert,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "चेतावनी: ऐप अपडेट न होने पर यह वर्जन काम नहीं करेगा और ऐप बंद (Crash/Exit) हो जाएगा।",
                                color = RedAlert,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Description and Varnan 2 Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(GlassSurfaceElevated)
                        .border(1.dp, GlassBorderCyan, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = updateInfo.version2Description,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        ) {
                            items(updateInfo.changelogPoints) { point ->
                                Text(
                                    text = point,
                                    color = if (point.startsWith("वर्णन")) AmberAccent else TextPrimary,
                                    fontWeight = if (point.startsWith("वर्णन")) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Action Button: Update via Telegram Channel
                Button(
                    onClick = onUpdateClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("update_via_telegram_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "अपडेट करें (POPO Telegram Channel)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Button: Exit App (if refused)
                OutlinedButton(
                    onClick = onExitClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("exit_app_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (updateInfo.isMandatory) RedAlert else TextSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (updateInfo.isMandatory) "ऐप बंद करें (Exit App)" else "बाद में (Later)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

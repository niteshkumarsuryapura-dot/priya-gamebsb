package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class QuickPrompt(
    val title: String,
    val hindiTitle: String,
    val prompt: String,
    val icon: ImageVector
)

val defaultQuickPrompts = listOf(
    QuickPrompt(
        title = "Explain Screen",
        hindiTitle = "स्क्रीन समझाओ",
        prompt = "Explain what is on my screen right now",
        icon = Icons.AutoMirrored.Filled.ScreenShare
    ),
    QuickPrompt(
        title = "Take a Photo",
        hindiTitle = "फ़ोटो खींचो",
        prompt = "Take a photo and explain it",
        icon = Icons.Default.CameraAlt
    ),
    QuickPrompt(
        title = "Who Created You?",
        hindiTitle = "तुम्हें किसने बनाया?",
        prompt = "hello Priya tumhen kisne banaya hai",
        icon = Icons.Default.EmojiEmotions
    ),
    QuickPrompt(
        title = "Share Screen",
        hindiTitle = "स्क्रीन शेयर करो",
        prompt = "Start screen sharing",
        icon = Icons.AutoMirrored.Filled.ScreenShare
    ),
    QuickPrompt(
        title = "POPO Channel",
        hindiTitle = "पोपो चैनल खोलो",
        prompt = "POPO Telegram चैनल खोलो",
        icon = Icons.AutoMirrored.Filled.Send
    ),
    QuickPrompt(
        title = "Open YouTube",
        hindiTitle = "यूट्यूब खोलो",
        prompt = "YouTube खोलो",
        icon = Icons.Default.SmartDisplay
    ),
    QuickPrompt(
        title = "Open WhatsApp",
        hindiTitle = "व्हाट्सएप खोलो",
        prompt = "WhatsApp खोलो",
        icon = Icons.AutoMirrored.Filled.Send
    ),
    QuickPrompt(
        title = "Daily Briefing",
        hindiTitle = "आज का दिन",
        prompt = "नमस्ते माया, आज का दिन कैसा है? मुझे एक प्रेरक विचार और आज का संक्षिप्त विवरण दो।",
        icon = Icons.Default.Today
    ),
    QuickPrompt(
        title = "Friendly Chat",
        hindiTitle = "दोस्ताना बातचीत",
        prompt = "नमस्ते माया! आप कैसी हैं? आज मुझे कोई दिलचस्प बात बताइए।",
        icon = Icons.Default.EmojiEmotions
    ),
    QuickPrompt(
        title = "Explain Science",
        hindiTitle = "विज्ञान समझाओ",
        prompt = "माया, क्वांटम कंप्यूटिंग या ब्लैक होल क्या है? सरल हिंदी में विस्तार से समझाइए।",
        icon = Icons.Default.Science
    ),
    QuickPrompt(
        title = "Code & Tech",
        hindiTitle = "कोडिंग और तकनीक",
        prompt = "माया, मुझे Android Kotlin में Coroutines और Flow का उपयोग एक उदाहरण के साथ समझाइए।",
        icon = Icons.Default.Code
    ),
    QuickPrompt(
        title = "Hindi Poem",
        hindiTitle = "सुंदर कविता",
        prompt = "माया, सफलता और उम्मीद पर एक सुंदर और प्रेरणादायक हिंदी कविता सुनाइए।",
        icon = Icons.Default.Create
    ),
    QuickPrompt(
        title = "Did You Know?",
        hindiTitle = "रोचक तथ्य",
        prompt = "माया, अंतरिक्ष और ब्रह्मांड से जुड़ा कोई अनोखा और रहस्यमयी तथ्य बताइए।",
        icon = Icons.Default.Lightbulb
    )
)

@Composable
fun QuickActionPanel(
    onPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        defaultQuickPrompts.forEachIndexed { index, item ->
            QuickPromptChip(
                item = item,
                onClick = { onPromptSelected(item.prompt) },
                modifier = Modifier.testTag("quick_prompt_chip_$index")
            )
        }
    }
}

@Composable
private fun QuickPromptChip(
    item: QuickPrompt,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(GlassSurfaceElevated)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = CyanPrimary,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = item.hindiTitle,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "• ${item.title}",
            color = TextSecondary,
            fontSize = 10.sp
        )
    }
}

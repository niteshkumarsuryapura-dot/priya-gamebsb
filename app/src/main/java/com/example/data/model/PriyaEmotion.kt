package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedAlert

enum class PriyaEmotion(
    val hindiTitle: String,
    val englishTitle: String,
    val emoji: String,
    val primaryColor: Color,
    val pitchMultiplier: Float,
    val speedMultiplier: Float
) {
    CALM(
        hindiTitle = "शांत एवं तत्पर",
        englishTitle = "Calm & Ready",
        emoji = "🌸",
        primaryColor = CyanPrimary,
        pitchMultiplier = 1.18f,
        speedMultiplier = 1.20f
    ),
    HAPPY(
        hindiTitle = "प्रसन्न",
        englishTitle = "Happy",
        emoji = "✨",
        primaryColor = GreenSuccess,
        pitchMultiplier = 1.24f,
        speedMultiplier = 1.25f
    ),
    THINKING(
        hindiTitle = "विश्लेषणरत",
        englishTitle = "Deep Analysis",
        emoji = "🤔",
        primaryColor = AmberAccent,
        pitchMultiplier = 1.18f,
        speedMultiplier = 1.15f
    ),
    EXCITED(
        hindiTitle = "उत्साहित",
        englishTitle = "Supercharged",
        emoji = "⚡",
        primaryColor = BlueAccent,
        pitchMultiplier = 1.26f,
        speedMultiplier = 1.28f
    ),
    FOCUSED_CODING(
        hindiTitle = "कोडिंग जीनियस",
        englishTitle = "Coding Genius",
        emoji = "💻",
        primaryColor = Color(0xFF00E676),
        pitchMultiplier = 1.19f,
        speedMultiplier = 1.16f
    ),
    CARING(
        hindiTitle = "स्नेहपूर्ण",
        englishTitle = "Caring & Warm",
        emoji = "💖",
        primaryColor = Color(0xFFFF4081),
        pitchMultiplier = 1.22f,
        speedMultiplier = 1.18f
    );

    companion object {
        fun detectFromContext(text: String, isProcessing: Boolean): PriyaEmotion {
            if (isProcessing) return THINKING

            val lower = text.lowercase()

            // Coding keywords check
            val codingKeywords = listOf(
                "code", "coding", "program", "kotlin", "java", "python", "javascript", "typescript",
                "c++", "cpp", "rust", "golang", "sql", "query", "database", "api", "function",
                "class", "bug", "error", "exception", "debug", "compile", "android", "compose",
                "algorithm", "dsa", "leetcode", "react", "html", "css", "node", "git",
                "कोडिंग", "कोड", "प्रोग्राम", "एल्गोरिदम", "एरर", "कंपाइल"
            )
            if (codingKeywords.any { lower.contains(it) }) {
                return FOCUSED_CODING
            }

            // Excitement / Vision / Tech keywords
            val excitedKeywords = listOf(
                "wow", "kya baat", "super", "shandar", "vision", "camera", "live cam",
                "jarvis", "ai", "magic", "amazing", "power", "supercharge", "स्क्रीन",
                "कैमरा", "शानदार", "कमाल", "लाइव"
            )
            if (excitedKeywords.any { lower.contains(it) }) {
                return EXCITED
            }

            // Caring / Welcoming
            val caringKeywords = listOf(
                "thank", "dhanyawad", "shukriya", "dhyan", "care", "kaisi ho", "help",
                "madad", "friend", "dost", "प्यार", "शुक्रिया", "धन्यवाद", "ख्याल"
            )
            if (caringKeywords.any { lower.contains(it) }) {
                return CARING
            }

            // Happy / Greeting
            val happyKeywords = listOf(
                "hello", "hi", "namaste", "joke", "chutkula", "smile", "khush",
                "नमस्ते", "हंसो", "मजा", "खुश"
            )
            if (happyKeywords.any { lower.contains(it) }) {
                return HAPPY
            }

            return CALM
        }
    }
}

package com.example.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.ui.graphics.vector.ImageVector

data class AppPermissionItem(
    val key: String,
    val name: String,
    val hindiName: String,
    val packageName: String?,
    val icon: ImageVector,
    val isEnabled: Boolean,
    val isAlwaysAllowed: Boolean = false
)

sealed class AppLaunchResult {
    data class Success(val appName: String, val message: String) : AppLaunchResult()
    data class PermissionDenied(val appName: String, val message: String) : AppLaunchResult()
    data class AppNotInstalled(val appName: String, val fallbackUrl: String?) : AppLaunchResult()
    data class Error(val message: String) : AppLaunchResult()
}

class AppLauncherManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("maya_app_permissions", Context.MODE_PRIVATE)

    companion object {
        const val POPO_TELEGRAM_URL = "https://t.me/+Yq1Sb6Jz9hc0MWFl"
        const val KEY_CUSTOM_TELEGRAM_CHANNEL = "custom_telegram_channel_id"

        const val KEY_POPO = "popo_channel"
        const val KEY_WHATSAPP = "whatsapp"
        const val KEY_YOUTUBE = "youtube"
        const val KEY_TELEGRAM = "telegram"
        const val KEY_CAMERA = "camera"
        const val KEY_PHONE = "phone"
        const val KEY_SETTINGS = "settings"
        const val KEY_BROWSER = "browser"
        const val KEY_GALLERY = "gallery"
        const val KEY_MAPS = "maps"
        const val KEY_CALCULATOR = "calculator"
        const val KEY_CLOCK = "clock"
        const val KEY_CALENDAR = "calendar"
        const val KEY_EMAIL = "email"
        const val KEY_PLAYSTORE = "playstore"
    }

    fun isAppAllowed(key: String): Boolean {
        if (key == KEY_POPO) return true // POPO Channel is always accessible
        return prefs.getBoolean("perm_$key", true) // Default true for ease of use, user can toggle
    }

    fun setAppAllowed(key: String, allowed: Boolean) {
        prefs.edit().putBoolean("perm_$key", allowed).apply()
    }

    fun getRawTelegramChannel(): String {
        return prefs.getString(KEY_CUSTOM_TELEGRAM_CHANNEL, "") ?: ""
    }

    fun setTelegramChannel(channelIdOrUrl: String) {
        prefs.edit().putString(KEY_CUSTOM_TELEGRAM_CHANNEL, channelIdOrUrl.trim()).apply()
    }

    fun getTelegramUrl(): String {
        val custom = getRawTelegramChannel()
        if (custom.isBlank()) {
            return POPO_TELEGRAM_URL
        }
        return when {
            custom.startsWith("http://") || custom.startsWith("https://") -> custom
            custom.startsWith("tg://") -> custom
            custom.startsWith("@") -> "https://t.me/${custom.substring(1)}"
            else -> "https://t.me/$custom"
        }
    }

    fun getSupportedApps(): List<AppPermissionItem> {
        return listOf(
            AppPermissionItem(
                key = KEY_POPO,
                name = "POPO Telegram Channel",
                hindiName = "पोपो टेलीग्राम चैनल",
                packageName = null,
                icon = Icons.Default.Send,
                isEnabled = true,
                isAlwaysAllowed = true
            ),
            AppPermissionItem(
                key = KEY_WHATSAPP,
                name = "WhatsApp",
                hindiName = "व्हाट्सएप",
                packageName = "com.whatsapp",
                icon = Icons.Default.Send,
                isEnabled = isAppAllowed(KEY_WHATSAPP)
            ),
            AppPermissionItem(
                key = KEY_YOUTUBE,
                name = "YouTube",
                hindiName = "यूट्यूब",
                packageName = "com.google.android.youtube",
                icon = Icons.Default.SmartDisplay,
                isEnabled = isAppAllowed(KEY_YOUTUBE)
            ),
            AppPermissionItem(
                key = KEY_TELEGRAM,
                name = "Telegram",
                hindiName = "टेलीग्राम",
                packageName = "org.telegram.messenger",
                icon = Icons.Default.Send,
                isEnabled = isAppAllowed(KEY_TELEGRAM)
            ),
            AppPermissionItem(
                key = KEY_CAMERA,
                name = "Camera",
                hindiName = "कैमरा",
                packageName = null,
                icon = Icons.Default.CameraAlt,
                isEnabled = isAppAllowed(KEY_CAMERA)
            ),
            AppPermissionItem(
                key = KEY_PHONE,
                name = "Phone / Dialer",
                hindiName = "फोन डायलर",
                packageName = null,
                icon = Icons.Default.Call,
                isEnabled = isAppAllowed(KEY_PHONE)
            ),
            AppPermissionItem(
                key = KEY_SETTINGS,
                name = "System Settings",
                hindiName = "सेटिंग्स",
                packageName = null,
                icon = Icons.Default.Settings,
                isEnabled = isAppAllowed(KEY_SETTINGS)
            ),
            AppPermissionItem(
                key = KEY_BROWSER,
                name = "Web Browser",
                hindiName = "वेब ब्राउज़र",
                packageName = null,
                icon = Icons.Default.Language,
                isEnabled = isAppAllowed(KEY_BROWSER)
            ),
            AppPermissionItem(
                key = KEY_GALLERY,
                name = "Gallery / Photos",
                hindiName = "फ़ोटो गैलरी",
                packageName = null,
                icon = Icons.Default.PhotoLibrary,
                isEnabled = isAppAllowed(KEY_GALLERY)
            ),
            AppPermissionItem(
                key = KEY_MAPS,
                name = "Google Maps",
                hindiName = "गूगल मैप्स",
                packageName = "com.google.android.apps.maps",
                icon = Icons.Default.Map,
                isEnabled = isAppAllowed(KEY_MAPS)
            ),
            AppPermissionItem(
                key = KEY_CALCULATOR,
                name = "Calculator",
                hindiName = "कैलकुलेटर",
                packageName = "com.google.android.calculator",
                icon = Icons.Default.Calculate,
                isEnabled = isAppAllowed(KEY_CALCULATOR)
            ),
            AppPermissionItem(
                key = KEY_CLOCK,
                name = "Clock / Alarm",
                hindiName = "घड़ी / अलार्म",
                packageName = "com.google.android.deskclock",
                icon = Icons.Default.Schedule,
                isEnabled = isAppAllowed(KEY_CLOCK)
            ),
            AppPermissionItem(
                key = KEY_CALENDAR,
                name = "Calendar",
                hindiName = "कैलेंडर",
                packageName = "com.google.android.calendar",
                icon = Icons.Default.CalendarMonth,
                isEnabled = isAppAllowed(KEY_CALENDAR)
            ),
            AppPermissionItem(
                key = KEY_EMAIL,
                name = "Gmail / Email",
                hindiName = "जीमेल",
                packageName = "com.google.android.gm",
                icon = Icons.Default.Mail,
                isEnabled = isAppAllowed(KEY_EMAIL)
            ),
            AppPermissionItem(
                key = KEY_PLAYSTORE,
                name = "Google Play Store",
                hindiName = "प्ले स्टोर",
                packageName = "com.android.vending",
                icon = Icons.Default.Shop,
                isEnabled = isAppAllowed(KEY_PLAYSTORE)
            )
        )
    }

    fun detectAndExecuteAppCommand(query: String): AppLaunchResult? {
        val lower = query.lowercase().trim()

        // 1. POPO Telegram Channel
        if (lower.contains("popo") || lower.contains("पोपो") || (lower.contains("telegram") && lower.contains("channel")) || lower.contains("चैनल खोलो") || lower.contains("join telegram")) {
            return launchPopoTelegram()
        }

        // 2. Open Website by URL or Domain (e.g., "open google.com", "open youtube.com", "open wikipedia.org")
        val websiteResult = detectAndLaunchWebsite(lower)
        if (websiteResult != null) {
            return websiteResult
        }

        // 3. WhatsApp
        if (lower.contains("whatsapp") || lower.contains("व्हाट्सएप") || lower.contains("व्हाट्सअप")) {
            return launchAppByKey(KEY_WHATSAPP, "WhatsApp") {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    true
                } else false
            }
        }

        // 4. YouTube
        if (lower.contains("youtube") || lower.contains("यूट्यूब") || lower.contains("यू ट्यूब") || lower.contains("yt")) {
            return launchAppByKey(KEY_YOUTUBE, "YouTube") {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    true
                } else {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                    true
                }
            }
        }

        // 5. Telegram
        if (lower.contains("telegram") || lower.contains("टेलीग्राम")) {
            return launchAppByKey(KEY_TELEGRAM, "Telegram") {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("org.telegram.messenger")
                    ?: context.packageManager.getLaunchIntentForPackage("org.telegram.plus")
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    true
                } else {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.telegram.org")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                    true
                }
            }
        }

        // 6. Camera (Opening camera app) - Note: Taking a photo is handled separately by explicit photo capture command
        if ((lower.contains("open camera") || lower.contains("camera kholo") || lower.contains("camera chalu") || lower.contains("कैमरा खोलो") || lower.contains("कैमरा चालू") || lower.contains("कैमरा ऑन")) &&
            !lower.contains("photo khincho") && !lower.contains("capture photo") && !lower.contains("take a photo") && !lower.contains("photo lo")
        ) {
            return launchCamera()
        }

        // 7. Phone / Dialer
        if (lower.contains("dialer") || lower.contains("phone") || lower.contains("फोन") || lower.contains("कॉल खोलो") || lower.contains("डायल")) {
            return launchAppByKey(KEY_PHONE, "Phone") {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                true
            }
        }

        // 8. Settings
        if (lower.contains("setting") || lower.contains("सेटिंग्स") || lower.contains("सेटिंग")) {
            return launchAppByKey(KEY_SETTINGS, "Settings") {
                val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
                true
            }
        }

        // 9. Gallery / Photos
        if (lower.contains("gallery") || lower.contains("गैलरी") || lower.contains("photos app") || lower.contains("फ़ोटो ऐप")) {
            return launchAppByKey(KEY_GALLERY, "Gallery") {
                val galleryIntent = Intent(Intent.ACTION_VIEW).apply {
                    type = "image/*"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(galleryIntent)
                true
            }
        }

        // 10. Web Browser
        if (lower.contains("browser") || lower.contains("chrome") || lower.contains("गूगल खोलो") || lower.contains("इंटरनेट") || lower.contains("ब्राउज़र")) {
            return launchAppByKey(KEY_BROWSER, "Browser") {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                true
            }
        }

        // 11. Maps
        if (lower.contains("maps") || lower.contains("मैप्स") || lower.contains("नक्शा") || lower.contains("रास्ता खोलो")) {
            return launchAppByKey(KEY_MAPS, "Google Maps") {
                val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(mapsIntent)
                true
            }
        }

        // 12. Calculator
        if (lower.contains("calculator") || lower.contains("कैलकुलेटर") || lower.contains("हिसाब")) {
            return launchAppByKey(KEY_CALCULATOR, "Calculator") {
                val calcIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.calculator")
                    ?: context.packageManager.getLaunchIntentForPackage("com.android.calculator2")
                if (calcIntent != null) {
                    calcIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(calcIntent)
                    true
                } else false
            }
        }

        // 13. Clock / Alarm
        if (lower.contains("alarm") || lower.contains("अलार्म") || lower.contains("clock kholo") || lower.contains("घड़ी")) {
            return launchAppByKey(KEY_CLOCK, "Clock") {
                val clockIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.deskclock")
                if (clockIntent != null) {
                    clockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(clockIntent)
                    true
                } else false
            }
        }

        // 14. Calendar
        if (lower.contains("calendar") || lower.contains("कैलेंडर")) {
            return launchAppByKey(KEY_CALENDAR, "Calendar") {
                val calIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.calendar")
                if (calIntent != null) {
                    calIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(calIntent)
                    true
                } else false
            }
        }

        // 15. Gmail / Email
        if (lower.contains("gmail") || lower.contains("email") || lower.contains("जीमेल") || lower.contains("ईमेल")) {
            return launchAppByKey(KEY_EMAIL, "Gmail") {
                val mailIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
                if (mailIntent != null) {
                    mailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(mailIntent)
                    true
                } else false
            }
        }

        // 16. Play Store
        if (lower.contains("play store") || lower.contains("playstore") || lower.contains("प्ले स्टोर")) {
            return launchAppByKey(KEY_PLAYSTORE, "Google Play Store") {
                val storeIntent = context.packageManager.getLaunchIntentForPackage("com.android.vending")
                if (storeIntent != null) {
                    storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(storeIntent)
                    true
                } else false
            }
        }

        return null
    }

    private fun detectAndLaunchWebsite(query: String): AppLaunchResult? {
        val words = query.split("\\s+".toRegex())

        // Check for common domains or URL formats
        for (w in words) {
            val clean = w.trim(',', '.', '!', '?', ';', ':', '"', '\'')
            val isExplicitWebsite = clean.startsWith("http://") || clean.startsWith("https://") ||
                    clean.endsWith(".com") || clean.endsWith(".org") || clean.endsWith(".in") ||
                    clean.endsWith(".net") || clean.endsWith(".io") || clean.endsWith(".co") ||
                    clean.endsWith(".edu") || clean.endsWith(".gov")

            if (isExplicitWebsite && clean.contains(".")) {
                val targetUrl = if (clean.startsWith("http://") || clean.startsWith("https://")) clean else "https://$clean"
                return launchWebsite(targetUrl, clean)
            }
        }

        // Check for keywords like "open website <name>" or "website kholo <name>"
        if (query.contains("open website") || query.contains("website kholo") || query.contains("वेबसाइट खोलो")) {
            val parts = query.replace("open website", "")
                .replace("website kholo", "")
                .replace("वेबसाइट खोलो", "")
                .trim()

            if (parts.isNotBlank()) {
                val domain = if (parts.contains(".")) parts else "$parts.com"
                val targetUrl = if (domain.startsWith("http")) domain else "https://$domain"
                return launchWebsite(targetUrl, domain)
            }
        }

        return null
    }

    fun launchWebsite(url: String, domainName: String): AppLaunchResult {
        return try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            AppLaunchResult.Success(
                appName = domainName,
                message = "जी, वेबसाइट $domainName खोल रही हूँ।"
            )
        } catch (e: Exception) {
            AppLaunchResult.Error("वेबसाइट खोलने में त्रुटि: ${e.message}")
        }
    }

    fun launchCamera(): AppLaunchResult {
        return launchAppByKey(KEY_CAMERA, "Camera") {
            val cameraIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(cameraIntent)
            true
        }
    }

    private fun launchAppByKey(
        key: String,
        appName: String,
        launcherAction: () -> Boolean
    ): AppLaunchResult {
        if (!isAppAllowed(key)) {
            return AppLaunchResult.PermissionDenied(
                appName = appName,
                message = "क्षमा करें, $appName खोलने की अनुमति बंद है। कृपया Permissions मेनू में अनुमति चालू करें।"
            )
        }

        return try {
            val opened = launcherAction()
            if (opened) {
                AppLaunchResult.Success(
                    appName = appName,
                    message = "जी, $appName खोल रही हूँ।"
                )
            } else {
                AppLaunchResult.AppNotInstalled(
                    appName = appName,
                    fallbackUrl = null
                )
            }
        } catch (e: Exception) {
            AppLaunchResult.Error("त्रुटि: ${e.message}")
        }
    }

    fun launchPopoTelegram(): AppLaunchResult {
        return try {
            val url = getTelegramUrl()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            AppLaunchResult.Success(
                appName = "POPO Telegram Channel",
                message = "जी, Telegram चैनल खोल रही हूँ..."
            )
        } catch (e: Exception) {
            AppLaunchResult.Error("चैनल खोलने में समस्या आई: ${e.message}")
        }
    }
}

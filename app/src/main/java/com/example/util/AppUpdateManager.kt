package com.example.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.exitProcess

data class AppUpdateInfo(
    val hasUpdate: Boolean,
    val currentVersion: String,
    val latestVersion: String,
    val isMandatory: Boolean,
    val title: String,
    val version2Description: String,
    val changelogPoints: List<String>,
    val telegramUpdateUrl: String = AppLauncherManager.POPO_TELEGRAM_URL,
    val directApkUrl: String? = null
)

class AppUpdateManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("maya_update_prefs", Context.MODE_PRIVATE)

    companion object {
        const val CURRENT_VERSION_NAME = "1.0.0 (v1)"
        const val CURRENT_VERSION_CODE = 1
        const val KEY_SIMULATE_V2_UPDATE = "simulate_v2_update"
        const val KEY_CUSTOM_UPDATE_URL = "custom_update_json_url"
    }

    private val _updateState = MutableStateFlow<AppUpdateInfo?>(null)
    val updateState: StateFlow<AppUpdateInfo?> = _updateState.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    fun isSimulatedUpdateEnabled(): Boolean {
        return prefs.getBoolean(KEY_SIMULATE_V2_UPDATE, false)
    }

    fun setSimulateUpdate(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SIMULATE_V2_UPDATE, enabled).apply()
        _updateState.value = if (enabled) getVersion2UpdateInfo(isMandatory = true) else null
    }

    /**
     * Checks if there's a new version (Version 2 or higher).
     * If remote update URL is provided or simulate mode is enabled, displays Update prompt.
     */
    suspend fun checkForUpdates(manualCheck: Boolean = false): AppUpdateInfo? {
        _isCheckingUpdate.value = true
        return withContext(Dispatchers.IO) {
            try {
                val simulateV2 = prefs.getBoolean(KEY_SIMULATE_V2_UPDATE, false)
                val customUrl = prefs.getString(KEY_CUSTOM_UPDATE_URL, "") ?: ""

                if (simulateV2) {
                    val info = getVersion2UpdateInfo(isMandatory = true)
                    _updateState.value = info
                    _isCheckingUpdate.value = false
                    return@withContext info
                }

                if (customUrl.isNotBlank()) {
                    val remoteInfo = fetchRemoteUpdateConfig(customUrl)
                    if (remoteInfo != null && remoteInfo.hasUpdate) {
                        _updateState.value = remoteInfo
                        _isCheckingUpdate.value = false
                        return@withContext remoteInfo
                    }
                }

                // Default: No pending forced update right now until version 2 is flagged
                _updateState.value = null
                _isCheckingUpdate.value = false
                null
            } catch (e: Exception) {
                _isCheckingUpdate.value = false
                null
            }
        }
    }

    private fun getVersion2UpdateInfo(isMandatory: Boolean = true): AppUpdateInfo {
        return AppUpdateInfo(
            hasUpdate = true,
            currentVersion = CURRENT_VERSION_NAME,
            latestVersion = "2.0.0 (v2.0 New Update)",
            isMandatory = isMandatory,
            title = "नया वर्जन 2.0 उपलब्ध है! (New Version 2 Update)",
            version2Description = "माया AI का नवीनतम 2.0 वर्जन रिलीज़ हो चुका है! इसमें अधिक तेज़ वॉयस इंजन, 'Hey Maya' वेक-वर्ड, नए ऐप कंट्रोल्स और बग फिक्स शामिल हैं। ऐप जारी रखने के लिए तुरंत टेलीग्राम चैनल से अपडेट डाउनलोड करें।",
            changelogPoints = listOf(
                "वर्णन २ (Version 2 Features):",
                "• ⚡ सुपरफास्ट रियल-टाइम Gemini AI इंटेलिजेंस",
                "• 🎙️ 'Hey Maya' वेक वर्ड बैकग्राउंड एक्टिवेशन",
                "• 🚀 WhatsApp, YouTube, Telegram डायरेक्ट ऐप ओपनर",
                "• 🛡️ प्राइवेसी एवं ग्रैन्युलर ऐप परमिशन मैनेजर",
                "• 📢 POPO टेलीग्राम ऑफिशियल कम्युनिटी डायरेक्ट सपोर्ट"
            ),
            telegramUpdateUrl = AppLauncherManager(context).getTelegramUrl()
        )
    }

    private fun fetchRemoteUpdateConfig(urlString: String): AppUpdateInfo? {
        return try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.requestMethod = "GET"
            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)
                val latestCode = json.optInt("versionCode", 1)
                val latestName = json.optString("versionName", "2.0.0")
                val isMandatory = json.optBoolean("isMandatory", true)
                val desc = json.optString("description", "New Version 2 update available")
                val teleUrl = json.optString("telegramUrl", AppLauncherManager.POPO_TELEGRAM_URL)

                if (latestCode > CURRENT_VERSION_CODE) {
                    val points = mutableListOf<String>()
                    val pointsArray = json.optJSONArray("changelog")
                    if (pointsArray != null) {
                        for (i in 0 until pointsArray.length()) {
                            points.add(pointsArray.getString(i))
                        }
                    } else {
                        points.addAll(listOf("• नया वर्शन 2.0 अपडेट", "• सुधार और नई सुविधाएं"))
                    }

                    AppUpdateInfo(
                        hasUpdate = true,
                        currentVersion = CURRENT_VERSION_NAME,
                        latestVersion = latestName,
                        isMandatory = isMandatory,
                        title = "Maya AI Version $latestName Available",
                        version2Description = desc,
                        changelogPoints = points,
                        telegramUpdateUrl = teleUrl
                    )
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Tapping on Update opens the Telegram channel to download the update APK.
     */
    fun openTelegramUpdate(telegramUrl: String? = null) {
        try {
            val url = telegramUrl ?: AppLauncherManager(context).getTelegramUrl()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * If update is mandatory and user refuses or cancels, close the app to enforce update.
     */
    fun exitAppOnRefuse(activity: Activity?) {
        if (activity != null) {
            activity.finishAffinity()
        } else {
            exitProcess(0)
        }
    }

    fun dismissNonMandatoryUpdate() {
        _updateState.value = null
    }
}

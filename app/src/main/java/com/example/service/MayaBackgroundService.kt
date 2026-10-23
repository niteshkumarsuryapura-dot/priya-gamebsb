package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import java.util.Locale

class MayaBackgroundService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isListening = false
    private var isDestroyed = false

    private val wakeWordPatterns = listOf(
        "hey maya", "hi maya", "hello maya", "ok maya", "oye maya",
        "हे माया", "हाय माया", "नमस्ते माया", "सुनो माया", "ओ माया", "maya", "माया"
    )

    private var isPausedForForeground = false

    companion object {
        const val CHANNEL_ID = "maya_wake_word_channel"
        const val NOTIFICATION_ID = 2026
        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val ACTION_PAUSE_LISTENING = "com.example.service.ACTION_PAUSE_LISTENING"
        const val ACTION_RESUME_LISTENING = "com.example.service.ACTION_RESUME_LISTENING"

        fun start(context: Context) {
            try {
                val intent = Intent(context, MayaBackgroundService::class.java).apply {
                    action = ACTION_START
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, MayaBackgroundService::class.java).apply {
                    action = ACTION_STOP
                }
                context.stopService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun pauseListening(context: Context) {
            try {
                val intent = Intent(context, MayaBackgroundService::class.java).apply {
                    action = ACTION_PAUSE_LISTENING
                }
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun resumeListening(context: Context) {
            try {
                val intent = Intent(context, MayaBackgroundService::class.java).apply {
                    action = ACTION_RESUME_LISTENING
                }
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
        initRecognizerOnMainThread()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_PAUSE_LISTENING -> {
                pauseForegroundListening()
                return START_STICKY
            }
            ACTION_RESUME_LISTENING -> {
                resumeForegroundListening()
                return START_STICKY
            }
        }

        val notification = buildForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)

        startContinuousListening()
        return START_STICKY
    }

    private fun pauseForegroundListening() {
        isPausedForForeground = true
        mainHandler.post {
            isListening = false
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
            speechRecognizer = null
        }
    }

    private fun resumeForegroundListening() {
        isPausedForForeground = false
        mainHandler.post {
            initRecognizerOnMainThread()
            startContinuousListening()
        }
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "MayaAI::WakeWordLock"
            ).apply {
                setReferenceCounted(false)
                acquire(24 * 60 * 60 * 1000L) // 24 hours
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Maya Voice Assistant (Wake Word)",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background listening service for 'Hey Maya'"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("माया वॉइस असिस्टेंट सक्रिय है 🎙️")
            .setContentText("बोलिए 'Hey Maya' किसी भी समय (Listening for 'Hey Maya')")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun initRecognizerOnMainThread() {
        mainHandler.post {
            if (isDestroyed) return@post
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {}

            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                try {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                        setRecognitionListener(createListener())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    speechRecognizer = null
                }
            }
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
            }

            override fun onError(error: Int) {
                isListening = false
                if (isDestroyed) return

                // Client error (5) or Recognizer busy (8): recreate recognizer cleanly
                if (error == SpeechRecognizer.ERROR_CLIENT || error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    mainHandler.post {
                        try {
                            speechRecognizer?.destroy()
                        } catch (_: Exception) {}
                        speechRecognizer = null
                        mainHandler.postDelayed({
                            initRecognizerOnMainThread()
                            startContinuousListening()
                        }, 800)
                    }
                    return
                }

                // Restart silently if not paused for foreground
                if (!isPausedForForeground && !isDestroyed) {
                    mainHandler.postDelayed({
                        startContinuousListening()
                    }, if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) 300 else 1000)
                }
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                if (isDestroyed) return

                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull { it.isNotBlank() }?.trim() ?: ""

                if (containsWakeWord(text)) {
                    val command = extractQueryFromWakeWord(text)
                    triggerWakeWordActivation(command)
                } else {
                    mainHandler.postDelayed({
                        startContinuousListening()
                    }, 400)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull { it.isNotBlank() }?.trim() ?: return

                if (containsWakeWord(partial)) {
                    val command = extractQueryFromWakeWord(partial)
                    triggerWakeWordActivation(command)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun startContinuousListening() {
        mainHandler.post {
            if (isDestroyed || isListening || isPausedForForeground) return@post

            if (speechRecognizer == null) {
                initRecognizerOnMainThread()
            }

            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "hi", "en-IN", "en-US"))
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }
                speechRecognizer?.startListening(intent)
                isListening = true
            } catch (_: Exception) {
                isListening = false
                mainHandler.postDelayed({
                    startContinuousListening()
                }, 1200)
            }
        }
    }

    private fun triggerWakeWordActivation(command: String) {
        try {
            // Wake up screen
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val screenLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "MayaAI::WakeScreenLock"
            )
            screenLock.acquire(4000L)

            // Launch MainActivity with ACTION_WAKE_WORD_ACTIVATED
            val launchIntent = Intent(this, MainActivity::class.java).apply {
                action = MainActivity.ACTION_WAKE_WORD_ACTIVATED
                putExtra(MainActivity.EXTRA_VOICE_COMMAND, command)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(launchIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Pause briefly before listening again so Maya's speech doesn't trigger wake word
        mainHandler.postDelayed({
            startContinuousListening()
        }, 3500)
    }

    private fun containsWakeWord(text: String): Boolean {
        val lower = text.lowercase(Locale.ROOT)
        return wakeWordPatterns.any { pattern -> lower.contains(pattern) }
    }

    private fun extractQueryFromWakeWord(text: String): String {
        var result = text.trim()
        val lower = result.lowercase(Locale.ROOT)
        for (pattern in wakeWordPatterns) {
            if (lower.startsWith(pattern)) {
                result = result.substring(pattern.length).trim().removePrefix(",").removePrefix(".").trim()
                return result
            }
        }
        for (pattern in wakeWordPatterns) {
            if (lower.contains(pattern)) {
                result = result.replace(Regex("(?i)\\b$pattern\\b"), "").trim()
            }
        }
        return result
    }

    override fun onDestroy() {
        isDestroyed = true
        isListening = false
        mainHandler.removeCallbacksAndMessages(null)
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null

        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}

        super.onDestroy()
    }
}

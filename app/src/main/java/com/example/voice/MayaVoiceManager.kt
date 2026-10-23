package com.example.voice

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class MayaVoiceManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false
    private var pendingSpeechText: String? = null
    private var lastCapturedPartialText: String? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isWakeWordEnabled = MutableStateFlow(false)
    val isWakeWordEnabled: StateFlow<Boolean> = _isWakeWordEnabled.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var onSpeechRecognized: ((String) -> Unit)? = null
    var onSpeakingFinished: (() -> Unit)? = null
    var onWakeWordDetected: (() -> Unit)? = null
    var onRecognitionError: ((String) -> Unit)? = null

    private var amplitudeJob: Job? = null
    private var restartListenerJob: Job? = null
    private val hindiLocale = Locale.forLanguageTag("hi-IN")
    private val englishLocale = Locale.forLanguageTag("en-IN")

    private val voicePrefs = context.getSharedPreferences("maya_voice_prefs", Context.MODE_PRIVATE)
    var currentSpeechRate: Float = voicePrefs.getFloat("speech_rate", 1.22f)
        private set
    var currentPitch: Float = voicePrefs.getFloat("speech_pitch", 1.20f)
        private set

    private val wakeWordPatterns = listOf(
        "hey priya", "hi priya", "hello priya", "ok priya", "oye priya", "priya", "प्रिया", "हे प्रिया", "हाय प्रिया", "नमस्ते प्रिया", "सुनो प्रिया",
        "hey maya", "hi maya", "hello maya", "ok maya", "oye maya",
        "हे माया", "हाय माया", "नमस्ते माया", "सुनो माया", "ओ माया", "maya", "माया"
    )

    init {
        initTts()
    }

    private fun initTts() {
        try {
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    configureFemaleHindiVoice()
                    pendingSpeechText?.let { queuedText ->
                        pendingSpeechText = null
                        scope.launch(Dispatchers.Main) {
                            speak(queuedText)
                        }
                    }
                } else {
                    _errorMessage.value = "TextToSpeech init status: $status"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _errorMessage.value = "TextToSpeech init error: ${e.message}"
        }
    }

    private fun configureFemaleHindiVoice() {
        val tts = textToSpeech ?: return
        try {
            // Set AudioAttributes for assistant/speech
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            tts.setAudioAttributes(audioAttributes)

            // Set Hindi Locale with fallback to en-IN / default
            val result = tts.setLanguage(hindiLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                val engResult = tts.setLanguage(englishLocale)
                if (engResult == TextToSpeech.LANG_MISSING_DATA || engResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.getDefault())
                }
            }

            // Strictly find female Hindi/Indian voice, explicitly rejecting any male voice
            val voices = tts.voices
            if (voices != null) {
                val nonMaleVoices = voices.filterNot { voice ->
                    val name = voice.name.lowercase()
                    name.contains("male") && !name.contains("female")
                }

                val femaleIndianVoice = nonMaleVoices.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    val loc = voice.locale
                    (loc.language == "hi" || loc.toLanguageTag() == "hi-IN" || loc.toLanguageTag() == "en-IN") &&
                            (name.contains("female") || name.contains("fem") || name.contains("woman") ||
                             name.contains("girl") || name.contains("f0") || name.contains("f-") ||
                             name.contains("hie") || name.contains("hid") || name.contains("ene"))
                } ?: nonMaleVoices.firstOrNull { voice ->
                    val loc = voice.locale
                    loc.language == "hi" || loc.toLanguageTag() == "hi-IN"
                } ?: nonMaleVoices.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    name.contains("female") || name.contains("fem") || name.contains("woman") || name.contains("girl")
                } ?: nonMaleVoices.firstOrNull { voice ->
                    val loc = voice.locale
                    loc.language == "en" && loc.country == "IN"
                }

                if (femaleIndianVoice != null) {
                    tts.voice = femaleIndianVoice
                }
            }

            // Set natural, crisp female pitch (1.20f) and fast, energetic speech cadence
            tts.setPitch(currentPitch)
            tts.setSpeechRate(currentSpeechRate)

            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    startSimulatedVoiceAmplitude()
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopAmplitude()
                    scope.launch(Dispatchers.Main) {
                        onSpeakingFinished?.invoke()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopAmplitude()
                    scope.launch(Dispatchers.Main) {
                        onSpeakingFinished?.invoke()
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    stopAmplitude()
                    scope.launch(Dispatchers.Main) {
                        onSpeakingFinished?.invoke()
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setWakeWordMode(enabled: Boolean) {
        _isWakeWordEnabled.value = enabled
        if (enabled && !_isListening.value && !_isSpeaking.value) {
            startListening()
        }
    }

    private fun initSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return
        }
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {}

            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createRecognitionListener())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                speechRecognizer = null
            }
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
                if (_isWakeWordEnabled.value) {
                    _liveTranscript.value = "सुन रही हूँ... 'Hey Priya' या 'Hey Maya' बोलें"
                } else {
                    _liveTranscript.value = "सुन रही हूँ... बोलिए (Listening...)"
                }
            }

            override fun onBeginningOfSpeech() {
                // Natural Voice Interruption: if assistant is speaking when user starts talking, stop TTS immediately!
                if (_isSpeaking.value) {
                    stopSpeaking()
                }
                _isListening.value = true
                _liveTranscript.value = "सुन रही हूँ..."
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Normalize rmsdB (-2 to ~10dB) to 0.1f..1f
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1f)
                _audioAmplitude.value = normalized
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
                _liveTranscript.value = "समझ रही हूँ... (Processing...)"
            }

            override fun onError(error: Int) {
                _isListening.value = false
                _audioAmplitude.value = 0f

                // If partial results already caught what user said before error/timeout occurred, process it!
                val recoveredText = lastCapturedPartialText?.trim()
                if (!recoveredText.isNullOrBlank() && recoveredText.length > 1) {
                    lastCapturedPartialText = null
                    handleRecognizedSpeech(recoveredText)
                    return
                }

                // If ERROR_CLIENT (5) or ERROR_RECOGNIZER_BUSY (8), safely reset recognizer on main thread
                if (error == SpeechRecognizer.ERROR_CLIENT || error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    mainHandler.post {
                        try {
                            speechRecognizer?.destroy()
                        } catch (_: Exception) {}
                        speechRecognizer = null
                    }
                }

                // If in continuous wake word mode, silently and quickly restart listener
                if (_isWakeWordEnabled.value && !_isSpeaking.value) {
                    val delay = if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) 300L else 1000L
                    scheduleWakeWordRestart(delayMs = delay)
                    return
                }

                val msg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "ऑडियो रिकॉर्डिंग में समस्या आई"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "माइक्रोफ़ोन परमिशन आवश्यक है"
                    SpeechRecognizer.ERROR_NETWORK -> "इंटरनेट कनेक्शन की आवश्यकता है"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "नेटवर्क टाइमआउट, कृपया दोबारा बोलें"
                    SpeechRecognizer.ERROR_NO_MATCH -> "आवाज़ स्पष्ट नहीं थी, कृपया दोबारा बोलें"
                    SpeechRecognizer.ERROR_SERVER -> "वॉइस सर्वर से संपर्क नहीं हो पाया"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "कोई आवाज़ नहीं मिली, कृपया बोलें"
                    SpeechRecognizer.ERROR_CLIENT, SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "माइक तैयार हो रहा है, कृपया बोलें"
                    else -> "कृपया दोबारा बोलें"
                }
                _errorMessage.value = msg
                onRecognitionError?.invoke(msg)
            }

            override fun onResults(results: Bundle?) {
                _isListening.value = false
                _audioAmplitude.value = 0f
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val rawText = matches?.firstOrNull { it.isNotBlank() }?.trim() ?: lastCapturedPartialText?.trim()
                lastCapturedPartialText = null

                if (!rawText.isNullOrBlank()) {
                    _liveTranscript.value = rawText
                    handleRecognizedSpeech(rawText)
                } else if (_isWakeWordEnabled.value && !_isSpeaking.value) {
                    scheduleWakeWordRestart(400)
                } else {
                    val msg = "कोई आवाज़ नहीं मिली, कृपया दोबारा बोलें"
                    _errorMessage.value = msg
                    onRecognitionError?.invoke(msg)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val partial = matches[0].trim()
                    if (partial.isNotBlank()) {
                        lastCapturedPartialText = partial
                        _liveTranscript.value = partial

                        // Check for early wake word trigger in real time
                        if (containsWakeWord(partial) && _isWakeWordEnabled.value) {
                            onWakeWordDetected?.invoke()
                        }
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun handleRecognizedSpeech(rawText: String) {
        if (_isWakeWordEnabled.value) {
            val cleanPrompt = extractQueryFromWakeWord(rawText)
            if (cleanPrompt.isNotBlank()) {
                onSpeechRecognized?.invoke(cleanPrompt)
            } else {
                onWakeWordDetected?.invoke()
            }
        } else {
            // Direct mic tap mode: pass user input to Maya
            val query = if (containsWakeWord(rawText)) {
                val extracted = extractQueryFromWakeWord(rawText)
                if (extracted.isNotBlank()) extracted else rawText
            } else {
                rawText
            }
            onSpeechRecognized?.invoke(query)
        }
    }

    fun createSpeechRecognizerIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "hi", "en-IN", "en-US", "en"))
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PROMPT, if (_isWakeWordEnabled.value) "Say 'Hey Maya'..." else "माया से बोलें... (Speak to Maya)")
        }
    }

    fun startListening() {
        mainHandler.post {
            stopSpeaking()
            restartListenerJob?.cancel()
            _errorMessage.value = null
            lastCapturedPartialText = null

            if (!_isWakeWordEnabled.value) {
                _liveTranscript.value = "सुन रही हूँ... बोलिए (Listening...)"
            }

            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                val msg = "Speech recognition is not available on this device."
                _errorMessage.value = msg
                onRecognitionError?.invoke(msg)
                return@post
            }

            if (speechRecognizer == null) {
                try {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createRecognitionListener())
                    }
                } catch (e: Exception) {
                    speechRecognizer = null
                    val msg = "रिकग्निशन शुरू करने में समस्या: ${e.message}"
                    _errorMessage.value = msg
                    onRecognitionError?.invoke(msg)
                    return@post
                }
            }

            try {
                val intent = createSpeechRecognizerIntent()
                speechRecognizer?.startListening(intent)
                _isListening.value = true
            } catch (e: Exception) {
                _isListening.value = false
                try {
                    speechRecognizer?.destroy()
                } catch (_: Exception) {}
                speechRecognizer = null
                if (_isWakeWordEnabled.value) {
                    scheduleWakeWordRestart(1000)
                }
            }
        }
    }

    private fun scheduleWakeWordRestart(delayMs: Long) {
        restartListenerJob?.cancel()
        restartListenerJob = scope.launch(Dispatchers.Main) {
            delay(delayMs)
            if (_isWakeWordEnabled.value && !_isSpeaking.value && !_isListening.value) {
                startListening()
            }
        }
    }

    private fun containsWakeWord(text: String): Boolean {
        val lower = text.lowercase()
        return wakeWordPatterns.any { pattern -> lower.contains(pattern) }
    }

    private fun extractQueryFromWakeWord(text: String): String {
        var result = text.trim()
        val lower = result.lowercase()
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

    fun stopListening() {
        restartListenerJob?.cancel()
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isListening.value = false
            _audioAmplitude.value = 0f
        }
    }

    fun speak(text: String, emotion: com.example.data.model.PriyaEmotion = com.example.data.model.PriyaEmotion.CALM) {
        stopListening()
        // Clean text for speech (strip markdown asterisks, hashes, backticks, urls)
        val spokenText = cleanTextForSpeech(text)
        if (spokenText.isBlank()) return

        val tts = textToSpeech
        if (!isTtsReady || tts == null) {
            pendingSpeechText = spokenText
            if (tts == null) {
                initTts()
            }
            return
        }

        // Unmute and ensure media audio volume is comfortably loud and audible
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager != null) {
                val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                if (maxVol > 0) {
                    val desiredVol = (maxVol * 0.85f).toInt().coerceAtLeast(1)
                    if (currentVol < desiredVol) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, desiredVol, 0)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Apply fast energetic speed and natural female pitch modulated by emotion
        val targetPitch = (currentPitch * emotion.pitchMultiplier / 1.20f).coerceIn(1.12f, 1.45f)
        val targetRate = (currentSpeechRate * emotion.speedMultiplier / 1.22f).coerceIn(1.05f, 1.40f)
        tts.setPitch(targetPitch)
        tts.setSpeechRate(targetRate)

        // Detect language script: Devanagari vs Latin
        val containsHindi = spokenText.any { it in '\u0900'..'\u097F' }
        val targetLocale = if (containsHindi) hindiLocale else englishLocale
        val langResult = tts.setLanguage(targetLocale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            val engResult = tts.setLanguage(englishLocale)
            if (engResult == TextToSpeech.LANG_MISSING_DATA || engResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setLanguage(Locale.getDefault())
            }
        }

        val utteranceId = "maya_speech_${System.currentTimeMillis()}"
        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }
        val result = tts.speak(spokenText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        if (result == TextToSpeech.SUCCESS) {
            _isSpeaking.value = true
            startSimulatedVoiceAmplitude()

            // Watchdog fallback: calculate estimated speech duration and ensure onSpeakingFinished fires
            // in case Android TTS engine fails to call onDone
            val estimatedSeconds = (spokenText.length / 10).coerceIn(4, 25)
            scope.launch(Dispatchers.Main) {
                delay(estimatedSeconds * 1000L + 2500L)
                if (_isSpeaking.value) {
                    _isSpeaking.value = false
                    stopAmplitude()
                    onSpeakingFinished?.invoke()
                }
            }
        } else {
            // Fallback retry with default locale
            try {
                tts.setLanguage(Locale.getDefault())
                val retry = tts.speak(spokenText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
                if (retry == TextToSpeech.SUCCESS) {
                    _isSpeaking.value = true
                    startSimulatedVoiceAmplitude()
                    return
                }
            } catch (_: Exception) {}

            _isSpeaking.value = false
            stopAmplitude()
            onSpeakingFinished?.invoke()
        }
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isSpeaking.value = false
            stopAmplitude()
        }
    }

    private fun startSimulatedVoiceAmplitude() {
        stopAmplitude()
        amplitudeJob = scope.launch(Dispatchers.Default) {
            var phase = 0f
            while (isActive && _isSpeaking.value) {
                // Generate natural voice wave modulation
                val wave = (kotlin.math.sin(phase) * 0.4f + kotlin.math.cos(phase * 2.3f) * 0.3f + 0.5f).coerceIn(0.2f, 1f)
                _audioAmplitude.value = wave
                phase += 0.35f
                delay(50)
            }
            _audioAmplitude.value = 0f
        }
    }

    private fun stopAmplitude() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        _audioAmplitude.value = 0f
    }

    private fun cleanTextForSpeech(input: String): String {
        return input
            .replace(Regex("[*#`_~>]"), "")
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
            .replace(Regex("https?://\\S+"), "link")
            .replace(Regex("\\n+"), ". ")
            .trim()
    }

    fun setSpeechRate(rate: Float) {
        val bounded = rate.coerceIn(0.7f, 2.2f)
        currentSpeechRate = bounded
        voicePrefs.edit().putFloat("speech_rate", bounded).apply()
        textToSpeech?.setSpeechRate(bounded)
    }

    fun setPitch(pitch: Float) {
        val bounded = pitch.coerceIn(0.7f, 2.0f)
        currentPitch = bounded
        voicePrefs.edit().putFloat("speech_pitch", bounded).apply()
        textToSpeech?.setPitch(bounded)
    }

    fun destroy() {
        stopSpeaking()
        stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}

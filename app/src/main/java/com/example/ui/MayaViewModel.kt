package com.example.ui

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.BuildConfig
import com.example.data.db.MayaDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.MayaMemory
import com.example.data.model.MayaState
import com.example.data.model.PriyaEmotion
import com.example.data.repository.MayaRepository
import com.example.screen.ScreenCaptureManager
import com.example.screen.ScreenCaptureService
import com.example.util.AppLaunchResult
import com.example.util.AppLauncherManager
import com.example.util.AppUpdateInfo
import com.example.util.AppUpdateManager
import com.example.voice.MayaVoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MayaViewModel(application: Application) : AndroidViewModel(application) {

    private val database: MayaDatabase = Room.databaseBuilder(
        application.applicationContext,
        MayaDatabase::class.java,
        "maya_ai_database"
    ).fallbackToDestructiveMigration(dropAllTables = true).build()

    private val repository = MayaRepository(database)
    val voiceManager = MayaVoiceManager(application.applicationContext, viewModelScope)
    val launcherManager = AppLauncherManager(application.applicationContext)
    val updateManager = AppUpdateManager(application.applicationContext)

    private val _currentEmotion = MutableStateFlow(PriyaEmotion.CALM)
    val currentEmotion: StateFlow<PriyaEmotion> = _currentEmotion.asStateFlow()

    // Live Voice Cam state
    private val _isLiveVoiceCamOpen = MutableStateFlow(false)
    val isLiveVoiceCamOpen: StateFlow<Boolean> = _isLiveVoiceCamOpen.asStateFlow()

    private val _isLiveCamFront = MutableStateFlow(false)
    val isLiveCamFront: StateFlow<Boolean> = _isLiveCamFront.asStateFlow()

    private val _isLiveCamAnalyzing = MutableStateFlow(false)
    val isLiveCamAnalyzing: StateFlow<Boolean> = _isLiveCamAnalyzing.asStateFlow()

    val pendingUpdate: StateFlow<AppUpdateInfo?> = updateManager.updateState
    val isCheckingUpdate: StateFlow<Boolean> = updateManager.isCheckingUpdate

    val messages: StateFlow<List<ChatMessage>> = repository.allMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val memories: StateFlow<List<MayaMemory>> = repository.allMemories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _mayaState = MutableStateFlow(MayaState.IDLE)
    val mayaState: StateFlow<MayaState> = _mayaState.asStateFlow()

    private val _lastResponse = MutableStateFlow("नमस्ते! मैं प्रिया (Priya AI) हूँ, आपकी JARVIS-स्टाइल AI साथी। कहिए मैं आपकी क्या मदद करूँ?")
    val lastResponse: StateFlow<String> = _lastResponse.asStateFlow()

    private val _showHistorySheet = MutableStateFlow(false)
    val showHistorySheet: StateFlow<Boolean> = _showHistorySheet.asStateFlow()

    private val _showMemorySheet = MutableStateFlow(false)
    val showMemorySheet: StateFlow<Boolean> = _showMemorySheet.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showPopoDialog = MutableStateFlow(false)
    val showPopoDialog: StateFlow<Boolean> = _showPopoDialog.asStateFlow()

    private val _showPermissionsDialog = MutableStateFlow(false)
    val showPermissionsDialog: StateFlow<Boolean> = _showPermissionsDialog.asStateFlow()

    private val _voicePitch = MutableStateFlow(voiceManager.currentPitch)
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _speechRate = MutableStateFlow(voiceManager.currentSpeechRate)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    // Screen sharing & Vision states
    val isScreenSharing: StateFlow<Boolean> = ScreenCaptureManager.isScreenSharing
    val lastCapturedScreen: StateFlow<Bitmap?> = ScreenCaptureManager.lastCapturedBitmap

    private val _capturedPhoto = MutableStateFlow<Bitmap?>(null)
    val capturedPhoto: StateFlow<Bitmap?> = _capturedPhoto.asStateFlow()

    private val _isAnalyzingVisual = MutableStateFlow(false)
    val isAnalyzingVisual: StateFlow<Boolean> = _isAnalyzingVisual.asStateFlow()

    // UI action callbacks
    var onRequestScreenCapturePermission: (() -> Unit)? = null
    var onRequestCameraCapture: (() -> Unit)? = null

    val liveTranscript = voiceManager.liveTranscript
    val audioAmplitude = voiceManager.audioAmplitude
    val voiceErrorMessage = voiceManager.errorMessage
    val isWakeWordActive = voiceManager.isWakeWordEnabled

    val hasApiKey: Boolean
        get() = BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    init {
        // Wire voice manager callbacks
        voiceManager.onSpeechRecognized = { recognizedSpeech ->
            if (recognizedSpeech.isNotBlank()) {
                processUserPrompt(recognizedSpeech, isVoice = true)
            }
        }

        voiceManager.onWakeWordDetected = {
            speakText("जी, कहिए! मैं सुन रही हूँ।")
        }

        voiceManager.onRecognitionError = { errMsg ->
            if (_mayaState.value == MayaState.PROCESSING || _mayaState.value == MayaState.LISTENING) {
                _mayaState.value = MayaState.IDLE
            }
            if (!errMsg.contains("client", ignoreCase = true) &&
                !errMsg.contains("busy", ignoreCase = true) &&
                !errMsg.contains("recognition", ignoreCase = true)
            ) {
                _lastResponse.value = errMsg
            }
        }

        voiceManager.onSpeakingFinished = {
            if (_mayaState.value == MayaState.SPEAKING) {
                _mayaState.value = MayaState.IDLE
                if (voiceManager.isWakeWordEnabled.value) {
                    voiceManager.startListening()
                }
            }
        }

        viewModelScope.launch {
            voiceManager.isListening.collect { listening ->
                if (listening) {
                    _mayaState.value = MayaState.LISTENING
                } else if (_mayaState.value == MayaState.LISTENING) {
                    _mayaState.value = MayaState.PROCESSING
                }
            }
        }

        viewModelScope.launch {
            voiceManager.isSpeaking.collect { speaking ->
                if (speaking) {
                    _mayaState.value = MayaState.SPEAKING
                } else if (_mayaState.value == MayaState.SPEAKING) {
                    _mayaState.value = MayaState.IDLE
                }
            }
        }

        // Seed initial welcoming memory if memories empty
        viewModelScope.launch {
            if (memories.value.isEmpty()) {
                repository.saveMemory(
                    key = "Assistant Identity",
                    value = "Priya (Maya) - Female Hindi & English JARVIS Multimodal AI Assistant with Screen Understanding",
                    category = "System"
                )
            }
        }

        // Check for updates on startup
        viewModelScope.launch {
            updateManager.checkForUpdates(manualCheck = false)
        }
    }

    fun triggerUpdateCheck() {
        viewModelScope.launch {
            val res = updateManager.checkForUpdates(manualCheck = true)
            if (res == null) {
                speakText("आप प्रिया AI का नवीनतम वर्जन उपयोग कर रहे हैं। कोई नया अपडेट अभी लंबित नहीं है।")
            }
        }
    }

    fun onUpdateRequested() {
        updateManager.openTelegramUpdate()
    }

    fun onRefuseUpdate(activity: android.app.Activity?) {
        updateManager.exitAppOnRefuse(activity)
    }

    fun dismissUpdate() {
        updateManager.dismissNonMandatoryUpdate()
    }

    fun onWakeWordTriggered() {
        val greeting = "जी, कहिए! मैं सुन रही हूँ।"
        _lastResponse.value = greeting
        _mayaState.value = MayaState.SPEAKING
        voiceManager.speak(greeting)
    }

    fun onMicClicked() {
        when (_mayaState.value) {
            MayaState.LISTENING -> {
                voiceManager.stopListening()
                _mayaState.value = MayaState.IDLE
            }
            MayaState.SPEAKING -> {
                voiceManager.stopSpeaking()
                _mayaState.value = MayaState.IDLE
            }
            else -> {
                _mayaState.value = MayaState.LISTENING
                voiceManager.startListening()
            }
        }
    }

    fun toggleWakeWordMode() {
        val next = !voiceManager.isWakeWordEnabled.value
        voiceManager.setWakeWordMode(next)
        if (next) {
            speakText("वेक वर्ड सक्रिय है। अब आप कभी भी 'Hey Priya' या 'Hey Maya' बोल सकते हैं।")
        }
    }

    // Screen Share Controls
    fun requestScreenSharing() {
        if (ScreenCaptureManager.isScreenSharing.value) {
            speakText("स्क्रीन शेयरिंग पहले से सक्रिय है। आप कभी भी स्क्रीन के बारे में पूछ सकते हैं।")
        } else {
            speakText("स्क्रीन शेयरिंग शुरू करने के लिए कृपया स्क्रीन रिकॉर्डिंग परमिशन स्वीकार करें।")
            onRequestScreenCapturePermission?.invoke()
        }
    }

    fun onScreenCapturePermissionGranted(resultCode: Int, resultData: Intent) {
        ScreenCaptureService.start(getApplication(), resultCode, resultData)
        speakText("स्क्रीन शेयरिंग शुरू हो गई है। आप स्क्रीन के बारे में कुछ भी पूछ सकते हैं, जैसे 'स्क्रीन पर क्या है' या 'Stop sharing' बोलकर इसे रोक सकते हैं।")
    }

    fun stopScreenSharing() {
        ScreenCaptureManager.stop()
        ScreenCaptureService.stop(getApplication())
        speakText("स्क्रीन शेयरिंग रोक दी गई है।")
    }

    fun explainCurrentScreen(userQuestion: String = "Explain what is on the screen right now") {
        if (!ScreenCaptureManager.isScreenSharing.value) {
            speakText("स्क्रीन को समझने के लिए पहले स्क्रीन शेयरिंग चालू करना आवश्यक है।")
            requestScreenSharing()
            return
        }

        val bitmap = ScreenCaptureManager.captureLatestScreenBitmap()
        if (bitmap == null) {
            speakText("स्क्रीन से तस्वीर प्राप्त नहीं हो सकी। कृपया दोबारा प्रयास करें।")
            return
        }

        _mayaState.value = MayaState.PROCESSING
        _isAnalyzingVisual.value = true

        viewModelScope.launch {
            val result = repository.explainScreen(bitmap, userQuestion, isVoice = true)
            _isAnalyzingVisual.value = false
            result.onSuccess { reply ->
                _lastResponse.value = reply
                _mayaState.value = MayaState.SPEAKING
                voiceManager.speak(reply)
            }.onFailure { err ->
                val fallback = "स्क्रीन का विश्लेषण करने में समस्या आई: ${err.message}"
                _lastResponse.value = fallback
                _mayaState.value = MayaState.ERROR
                voiceManager.speak(fallback)
            }
        }
    }

    // Camera & Photo Controls
    fun requestPhotoCapture() {
        speakText("कैमरा खोला जा रहा है, कृपया फ़ोटो खींचें।")
        onRequestCameraCapture?.invoke()
    }

    fun onPhotoCaptured(bitmap: Bitmap, prompt: String = "Explain what is in this captured photo") {
        _capturedPhoto.value = bitmap
        _mayaState.value = MayaState.PROCESSING
        _isAnalyzingVisual.value = true

        viewModelScope.launch {
            val result = repository.explainPhoto(bitmap, prompt, isVoice = true)
            _isAnalyzingVisual.value = false
            result.onSuccess { reply ->
                _lastResponse.value = reply
                _mayaState.value = MayaState.SPEAKING
                voiceManager.speak(reply)
            }.onFailure { err ->
                val fallback = "फ़ोटो का विश्लेषण करने में समस्या आई: ${err.message}"
                _lastResponse.value = fallback
                _mayaState.value = MayaState.ERROR
                voiceManager.speak(fallback)
            }
        }
    }

    fun clearCapturedPhoto() {
        _capturedPhoto.value = null
    }

    // Live Voice Cam Controls
    fun openLiveVoiceCam() {
        _isLiveVoiceCamOpen.value = true
        _currentEmotion.value = PriyaEmotion.EXCITED
        speakText("लाइव वॉइस कैमरा सक्रिय हो गया है। आप किसी भी वस्तु, कोड या समस्या को कैमरे के सामने रखकर पूछ सकते हैं।", emotion = PriyaEmotion.EXCITED)
    }

    fun closeLiveVoiceCam() {
        _isLiveVoiceCamOpen.value = false
        _isLiveCamAnalyzing.value = false
        _currentEmotion.value = PriyaEmotion.CALM
        speakText("लाइव वॉइस कैमरा बंद कर दिया गया है।", emotion = PriyaEmotion.CALM)
    }

    fun toggleLiveCamLens() {
        _isLiveCamFront.value = !_isLiveCamFront.value
    }

    fun analyzeLiveCamFrame(bitmap: Bitmap, prompt: String = "Explain what is visible through the camera and answer in detail") {
        if (_isLiveCamAnalyzing.value) return
        _isLiveCamAnalyzing.value = true
        _currentEmotion.value = PriyaEmotion.THINKING
        _mayaState.value = MayaState.PROCESSING

        viewModelScope.launch {
            val result = repository.explainLiveCamFrame(bitmap, prompt, isVoice = true)
            _isLiveCamAnalyzing.value = false
            result.onSuccess { reply ->
                val detected = PriyaEmotion.detectFromContext(reply, isProcessing = false)
                _currentEmotion.value = detected
                _lastResponse.value = reply
                _mayaState.value = MayaState.SPEAKING
                voiceManager.speak(reply, emotion = detected)
            }.onFailure { err ->
                val fallback = "कैमरा दृश्य का विश्लेषण करने में समस्या आई: ${err.message}"
                _currentEmotion.value = PriyaEmotion.CALM
                _lastResponse.value = fallback
                _mayaState.value = MayaState.ERROR
                voiceManager.speak(fallback, emotion = PriyaEmotion.CALM)
            }
        }
    }

    fun processUserPrompt(prompt: String, isVoice: Boolean = true) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank()) return

        val lower = trimmed.lowercase()

        // 1. Creator Query ("hello Maya tumhen kisne banaya hai", "hello Priya tumhen kisne banaya hai", etc.)
        val isCreatorQuery = (lower.contains("kisne") && (lower.contains("banaya") || lower.contains("banaaya") || lower.contains("bnaya"))) ||
                lower.contains("tumhe kisne banaya") ||
                lower.contains("tumhen kisne banaya") ||
                lower.contains("who made you") ||
                lower.contains("who created you") ||
                lower.contains("who is your creator") ||
                lower.contains("who is your developer") ||
                lower.contains("who made maya") ||
                lower.contains("who made priya") ||
                lower.contains("tumhara malik") ||
                lower.contains("nirmata kaun") ||
                lower.contains("तुम्हें किसने बनाया") ||
                lower.contains("किसने बनाया") ||
                lower.contains("किसने बनाया है")

        if (isCreatorQuery) {
            val reply = "नमस्ते! मुझे नितेश कुमार (Nitesh Kumar) ने बनाया है। मैं आपके लिए हमारा ऑफिशियल POPO Telegram चैनल खोल रही हूँ!"
            viewModelScope.launch {
                repository.saveMessage(trimmed, isUser = true, isVoice = isVoice)
                repository.saveMessage(reply, isUser = false, isVoice = isVoice)
                _lastResponse.value = reply
                _mayaState.value = MayaState.SPEAKING
                voiceManager.speak(reply)

                try {
                    val telegramIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+Yq1Sb6Jz9hc0MWFl")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    getApplication<Application>().startActivity(telegramIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return
        }

        // 2. Stop Screen Sharing command
        if (lower.contains("stop screen") || lower.contains("stop sharing") ||
            lower.contains("screen share band") || lower.contains("screen share roko") ||
            lower.contains("स्क्रीन शेयर बंद") || lower.contains("स्क्रीन शेयरिंग बंद") ||
            lower.contains("screen band karo")
        ) {
            stopScreenSharing()
            return
        }

        // 3. Start Screen Sharing command
        if ((lower.contains("start screen") || lower.contains("share screen") || lower.contains("screen share") || lower.contains("स्क्रीन शेयर") || lower.contains("स्क्रीन दिखाओ")) &&
            !lower.contains("band") && !lower.contains("stop") && !lower.contains("roko")
        ) {
            requestScreenSharing()
            return
        }

        // 4. Understand / Explain Screen command
        if (lower.contains("what's on my screen") || lower.contains("explain screen") ||
            lower.contains("explain my screen") || lower.contains("screen samjhao") ||
            lower.contains("screen par kya") || lower.contains("screen dekho") ||
            lower.contains("स्क्रीन पर क्या") || lower.contains("स्क्रीन समझाओ") ||
            lower.contains("स्क्रीन देखो") || lower.contains("read screen")
        ) {
            explainCurrentScreen(trimmed)
            return
        }

        // 5. Capture Photo command (Explicit)
        if (lower.contains("take a photo") || lower.contains("take photo") ||
            lower.contains("capture photo") || lower.contains("photo khincho") ||
            lower.contains("photo kheencho") || lower.contains("photo lo") ||
            lower.contains("फोटो खींचो") || lower.contains("फोटो लो") ||
            lower.contains("तस्वीर लो") || lower.contains("take picture")
        ) {
            requestPhotoCapture()
            return
        }

        // 6. Name / Greeting call out
        if (lower == "priya" || lower == "प्रिया" || lower == "hey priya" || lower == "हे प्रिया" || lower == "सुनो प्रिया" || lower == "hi priya" ||
            lower == "maya" || lower == "माया" || lower == "hey maya" || lower == "हे माया" || lower == "सुनो माया" || lower == "hi maya"
        ) {
            val greeting = "जी नमस्ते! मैं सुन रही हूँ। बताइए आज मैं आपकी क्या सहायता करूँ?"
            viewModelScope.launch {
                repository.saveMessage(trimmed, isUser = true, isVoice = isVoice)
                repository.saveMessage(greeting, isUser = false, isVoice = isVoice)
                _lastResponse.value = greeting
                _mayaState.value = MayaState.SPEAKING
                voiceManager.speak(greeting)
            }
            return
        }

        // 7. Voice speed / volume adjustments
        if (lower.contains("stage karo") ||
            (lower.contains("slow") && (lower.contains("tez") || lower.contains("stage") || lower.contains("badhao"))) ||
            ((lower.contains("tez") || lower.contains("तेज़") || lower.contains("तेज") || lower.contains("फास्ट") || lower.contains("fast") || lower.contains("speed badha")) &&
             (lower.contains("awaaz") || lower.contains("aawaz") || lower.contains("आवाज़") || lower.contains("bolo") || lower.contains("बोल") || lower.contains("karo") || lower.contains("करो")))
        ) {
            val newRate = (_speechRate.value + 0.25f).coerceAtMost(1.8f)
            setSpeechRate(newRate)
            val reply = "जी! मैंने आवाज़ और बोलने की गति तेज़ कर दी है। अब मैं तेज़ और स्पष्ट बोलूँगी।"
            viewModelScope.launch {
                repository.saveMessage(trimmed, isUser = true, isVoice = isVoice)
                repository.saveMessage(reply, isUser = false, isVoice = isVoice)
                _lastResponse.value = reply
                _mayaState.value = MayaState.SPEAKING
                voiceManager.speak(reply)
            }
            return
        }

        if ((lower.contains("dhire") || lower.contains("धीरे") || lower.contains("slow")) &&
            (lower.contains("bolo") || lower.contains("बोल") || lower.contains("karo") || lower.contains("करो") || lower.contains("speed"))
        ) {
            val newRate = (_speechRate.value - 0.25f).coerceAtLeast(0.85f)
            setSpeechRate(newRate)
            val reply = "जी! मैंने बोलने की गति सामान्य और धीमी कर दी है।"
            viewModelScope.launch {
                repository.saveMessage(trimmed, isUser = true, isVoice = isVoice)
                repository.saveMessage(reply, isUser = false, isVoice = isVoice)
                _lastResponse.value = reply
                _mayaState.value = MayaState.SPEAKING
                voiceManager.speak(reply)
            }
            return
        }

        // Live Voice Cam commands
        if (lower.contains("close camera") || lower.contains("camera band") || lower.contains("कैमरा बंद") || lower.contains("close cam")) {
            closeLiveVoiceCam()
            return
        }

        if (lower.contains("live camera") || lower.contains("live cam") || lower.contains("voice camera") ||
            lower.contains("camera kholo") || lower.contains("camera chalu") || lower.contains("कैमरा खोलो") ||
            lower.contains("कैमरा चालू") || lower.contains("सामने देखो") || lower.contains("live vision")
        ) {
            openLiveVoiceCam()
            return
        }

        // 8. Check for quick App Launcher / Website commands
        val appResult = launcherManager.detectAndExecuteAppCommand(trimmed)
        if (appResult != null) {
            handleAppLaunchResult(trimmed, appResult, isVoice)
            return
        }

        // 9. Process via Gemini AI repository with coding genius & emotion detection
        val detectedEmotion = PriyaEmotion.detectFromContext(trimmed, isProcessing = false)
        _currentEmotion.value = PriyaEmotion.THINKING
        _mayaState.value = MayaState.PROCESSING
        viewModelScope.launch {
            val result = repository.askMaya(trimmed, isVoice = isVoice)
            result.onSuccess { reply ->
                val replyEmotion = PriyaEmotion.detectFromContext(reply, isProcessing = false)
                val finalEmotion = if (detectedEmotion == PriyaEmotion.FOCUSED_CODING) PriyaEmotion.FOCUSED_CODING else replyEmotion
                _currentEmotion.value = finalEmotion
                _lastResponse.value = reply
                _mayaState.value = MayaState.SPEAKING
                voiceManager.speak(reply, emotion = finalEmotion)
            }.onFailure { error ->
                val fallback = error.message ?: "क्षमा करें, अनुरोध संसाधित करने में त्रुटि हुई।"
                _currentEmotion.value = PriyaEmotion.CALM
                _lastResponse.value = fallback
                _mayaState.value = MayaState.ERROR
                voiceManager.speak(fallback, emotion = PriyaEmotion.CALM)
            }
        }
    }

    private fun handleAppLaunchResult(prompt: String, result: AppLaunchResult, isVoice: Boolean) {
        viewModelScope.launch {
            repository.saveMessage(prompt, isUser = true, isVoice = isVoice)
            val replyText = when (result) {
                is AppLaunchResult.Success -> result.message
                is AppLaunchResult.PermissionDenied -> result.message
                is AppLaunchResult.AppNotInstalled -> "यह ऐप फोन में इनस्टॉल नहीं है।"
                is AppLaunchResult.Error -> result.message
            }
            repository.saveMessage(replyText, isUser = false, isVoice = isVoice)
            _lastResponse.value = replyText
            _mayaState.value = MayaState.SPEAKING
            voiceManager.speak(replyText, emotion = PriyaEmotion.HAPPY)
        }
    }

    fun openPopoTelegram() {
        val res = launcherManager.launchPopoTelegram()
        if (res is AppLaunchResult.Success) {
            speakText(res.message, emotion = PriyaEmotion.EXCITED)
        }
    }

    fun speakText(text: String, emotion: PriyaEmotion = _currentEmotion.value) {
        _mayaState.value = MayaState.SPEAKING
        voiceManager.speak(text, emotion = emotion)
    }

    fun stopAllSpeech() {
        voiceManager.stopSpeaking()
        voiceManager.stopListening()
        _mayaState.value = MayaState.IDLE
    }

    fun saveMemory(key: String, value: String, category: String = "Personal") {
        viewModelScope.launch {
            repository.saveMemory(key, value, category)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun setPitch(pitch: Float) {
        _voicePitch.value = pitch
        voiceManager.setPitch(pitch)
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
        voiceManager.setSpeechRate(rate)
    }

    fun testVoice() {
        speakText("नमस्ते! मैं प्रिया हूँ। मेरी आवाज़ अब बिल्कुल सही और स्पष्ट है।")
    }

    fun setShowHistorySheet(show: Boolean) {
        _showHistorySheet.value = show
    }

    fun setShowMemorySheet(show: Boolean) {
        _showMemorySheet.value = show
    }

    fun setShowSettingsDialog(show: Boolean) {
        _showSettingsDialog.value = show
    }

    fun setShowPopoDialog(show: Boolean) {
        _showPopoDialog.value = show
    }

    fun setShowPermissionsDialog(show: Boolean) {
        _showPermissionsDialog.value = show
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
        ScreenCaptureManager.stop()
    }
}

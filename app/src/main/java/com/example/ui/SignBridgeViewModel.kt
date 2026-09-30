package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiSignTranslator
import com.example.audio.AccessibilityFeedbackManager
import com.example.audio.SignAudioBridge
import com.example.data.SignDatabase
import com.example.data.SignEntity
import com.example.data.SignRepository
import com.example.data.TranslationHistoryEntity
import com.example.model.DetectedSign
import com.example.model.EmotionType
import com.example.model.FaceLandmarks
import com.example.model.HandLandmarks
import com.example.model.LiveTranslationState
import com.example.model.NavigationTab
import com.example.model.PoseLandmarks
import com.example.model.SignLanguage
import com.example.model.VisemeType
import com.example.vision.SignVisionEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SignBridgeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SignDatabase.getDatabase(application, viewModelScope)
    val repository = SignRepository(database.signDao())

    val visionEngine = SignVisionEngine()
    val audioBridge = SignAudioBridge(application)
    val feedbackManager = AccessibilityFeedbackManager(application)
    val geminiTranslator = GeminiSignTranslator()

    // Navigation Tab
    private val _currentTab = MutableStateFlow(NavigationTab.SIGN_TO_SPEECH)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    // Active Sign Language
    private val _activeLanguage = MutableStateFlow(SignLanguage.ASL)
    val activeLanguage: StateFlow<SignLanguage> = _activeLanguage.asStateFlow()

    // Live Sign-To-Speech translation state
    private val _translationState = MutableStateFlow(LiveTranslationState())
    val translationState: StateFlow<LiveTranslationState> = _translationState.asStateFlow()

    // Speech-To-Sign input & avatar animation sequence
    private val _spokenInputText = MutableStateFlow("")
    val spokenInputText: StateFlow<String> = _spokenInputText.asStateFlow()

    private val _avatarGlosses = MutableStateFlow<List<String>>(listOf("HELLO"))
    val avatarGlosses: StateFlow<List<String>> = _avatarGlosses.asStateFlow()

    // Privacy & Cloud AI enhancement toggles
    private val _preferOnDeviceProcessing = MutableStateFlow(true)
    val preferOnDeviceProcessing: StateFlow<Boolean> = _preferOnDeviceProcessing.asStateFlow()

    // Database flow bindings
    val availableSigns: StateFlow<List<SignEntity>> = repository.getSignsForLanguage("ASL")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customSigns: StateFlow<List<SignEntity>> = repository.getCustomSigns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val translationHistory: StateFlow<List<TranslationHistoryEntity>> = repository.getTranslationHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var grammarRefineJob: Job? = null

    init {
        // Observe newly detected signs from vision engine
        viewModelScope.launch {
            visionEngine.lastDetectedSign.collect { detected ->
                if (detected != null) {
                    handleNewDetectedSign(detected)
                }
            }
        }

        // Observe emotion changes from vision engine
        viewModelScope.launch {
            visionEngine.currentEmotion.collect { emotion ->
                _translationState.value = _translationState.value.copy(emotion = emotion)
            }
        }

        // Observe viseme changes from vision engine
        viewModelScope.launch {
            visionEngine.currentViseme.collect { viseme ->
                _translationState.value = _translationState.value.copy(currentViseme = viseme)
            }
        }

        // Observe audio TTS speaking state
        viewModelScope.launch {
            audioBridge.isSpeaking.collect { isSpeaking ->
                _translationState.value = _translationState.value.copy(isSpeakingAudio = isSpeaking)
            }
        }
    }

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun setLanguage(language: SignLanguage) {
        _activeLanguage.value = language
    }

    fun toggleOnDevicePreference() {
        _preferOnDeviceProcessing.value = !_preferOnDeviceProcessing.value
    }

    /**
     * Handles newly classified sign from vision engine
     */
    private fun handleNewDetectedSign(sign: DetectedSign) {
        val currentGlosses = _translationState.value.recognizedGlosses.toMutableList()

        // Debounce if last gloss is the same
        if (currentGlosses.isNotEmpty() && currentGlosses.last() == sign.gloss) {
            return
        }

        currentGlosses.add(sign.gloss)
        feedbackManager.vibrateSignRecognized()

        // Emergency sign detected? Trigger urgent haptic
        if (sign.gloss in listOf("HELP", "EMERGENCY", "DOCTOR")) {
            feedbackManager.vibrateUrgentAlert()
        }

        _translationState.value = _translationState.value.copy(
            recognizedGlosses = currentGlosses,
            currentSign = sign,
            confidence = sign.confidence,
            isGrammarRefining = true
        )

        // Increment database usage stats
        viewModelScope.launch {
            repository.incrementSignUsage(sign.gloss, sign.signLanguage.code)
        }

        // Refine with NLP / Gemini
        grammarRefineJob?.cancel()
        grammarRefineJob = viewModelScope.launch {
            delay(400) // Brief debounce
            val refined = geminiTranslator.refineSignGlossesToSpeech(
                glosses = currentGlosses,
                signLanguage = _activeLanguage.value,
                emotion = sign.emotion
            )

            _translationState.value = _translationState.value.copy(
                naturalSentence = refined,
                isGrammarRefining = false
            )

            // Record to persistent translation history
            repository.recordTranslation(
                sourceMode = "SIGN_TO_SPEECH",
                rawGlosses = currentGlosses.joinToString(" "),
                translatedSentence = refined,
                signLanguage = sign.signLanguage.code,
                emotion = sign.emotion.displayName,
                confidence = sign.confidence
            )
        }
    }

    /**
     * Manual / simulated gesture trigger (allows testing all signs from UI)
     */
    fun triggerSignGesture(gloss: String) {
        visionEngine.simulateSignGesture(gloss, _activeLanguage.value)
    }

    /**
     * Clear active sign sequence
     */
    fun clearGlosses() {
        _translationState.value = _translationState.value.copy(
            recognizedGlosses = emptyList(),
            currentSign = null,
            naturalSentence = "",
            isGrammarRefining = false
        )
    }

    /**
     * Speak current translation aloud using TTS
     */
    fun speakCurrentTranslation() {
        val sentence = _translationState.value.naturalSentence
        if (sentence.isNotBlank()) {
            audioBridge.speak(sentence, _activeLanguage.value)
        }
    }

    /**
     * Hearing person voice input -> Converts speech to 3D Avatar Sign sequence
     */
    fun setSpokenInput(text: String) {
        _spokenInputText.value = text
    }

    fun translateSpokenTextToSign() {
        val text = _spokenInputText.value
        if (text.isBlank()) return

        feedbackManager.vibrateSpeechDetected()

        viewModelScope.launch {
            val glosses = geminiTranslator.convertSpeechToSignGlosses(
                spokenText = text,
                targetLanguage = _activeLanguage.value
            )
            _avatarGlosses.value = glosses

            repository.recordTranslation(
                sourceMode = "SPEECH_TO_SIGN",
                rawGlosses = glosses.joinToString(" "),
                translatedSentence = text,
                signLanguage = _activeLanguage.value.code,
                emotion = "Spoken Hearing Input",
                confidence = 0.98f
            )
        }
    }

    /**
     * Emergency SOS Broadcast
     */
    fun triggerEmergencySos() {
        feedbackManager.vibrateUrgentAlert()
        val sosGlosses = listOf("HELP", "I AM DEAF", "EMERGENCY")
        _translationState.value = _translationState.value.copy(
            recognizedGlosses = sosGlosses,
            naturalSentence = "EMERGENCY! I am deaf and need immediate assistance!",
            emotion = EmotionType.URGENCY,
            confidence = 1.0f
        )
        audioBridge.speak("Emergency! I am deaf and need immediate assistance!", _activeLanguage.value)
    }

    /**
     * Record and save a custom personalized sign
     */
    fun saveCustomSign(
        gloss: String,
        translation: String,
        category: String,
        description: String
    ) {
        viewModelScope.launch {
            repository.insertSign(
                SignEntity(
                    gloss = gloss.trim().uppercase(),
                    translation = translation.trim(),
                    category = category,
                    signLanguage = _activeLanguage.value.code,
                    datasetSource = "Personalized / Custom",
                    description = description,
                    handShapeHint = "Personalized custom gesture recorded by user",
                    isCustom = true
                )
            )
        }
    }

    fun deleteCustomSign(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomSign(id)
        }
    }

    fun clearTranslationHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioBridge.shutdown()
    }
}

package com.example.model

data class DetectedSign(
    val id: String = java.util.UUID.randomUUID().toString(),
    val gloss: String,
    val textTranslation: String,
    val confidence: Float = 0.92f,
    val emotion: EmotionType = EmotionType.NEUTRAL,
    val signLanguage: SignLanguage = SignLanguage.ASL,
    val datasetSource: String = "WLASL",
    val timestamp: Long = System.currentTimeMillis()
)

data class LiveTranslationState(
    val recognizedGlosses: List<String> = emptyList(),
    val currentSign: DetectedSign? = null,
    val naturalSentence: String = "",
    val isTranslating: Boolean = false,
    val isGrammarRefining: Boolean = false,
    val confidence: Float = 0.88f,
    val emotion: EmotionType = EmotionType.NEUTRAL,
    val currentViseme: VisemeType = VisemeType.REST,
    val lastSpokenText: String = "",
    val isSpeakingAudio: Boolean = false
)

enum class NavigationTab(val label: String, val testTag: String) {
    SIGN_TO_SPEECH("Signer", "tab_sign_to_speech"),
    SPEECH_TO_SIGN("Voice to Sign", "tab_speech_to_sign"),
    DUAL_CONVERSATION("Dual Face", "tab_dual_conversation"),
    DICTIONARY("Sign Studio", "tab_dictionary"),
    SETTINGS("Privacy & AI", "tab_settings")
}

package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.model.SignLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SignAudioBridge(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _speechPitch = MutableStateFlow(1.0f)
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("SignAudioBridge", "Failed to init TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.language = Locale.US
            tts?.setSpeechRate(_speechRate.value)
            tts?.setPitch(_speechPitch.value)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        }
    }

    fun speak(text: String, signLanguage: SignLanguage = SignLanguage.ASL) {
        if (!isInitialized || tts == null || text.isBlank()) return

        // Set locale matching regional dialect if available
        val targetLocale = when (signLanguage) {
            SignLanguage.ASL -> Locale.US
            SignLanguage.BSL -> Locale.UK
            SignLanguage.ISL -> Locale.forLanguageTag("en-IN")
            SignLanguage.JSL -> Locale.JAPAN
            SignLanguage.KSL -> Locale.KOREA
            SignLanguage.AUSLAN -> Locale.forLanguageTag("en-AU")
        }

        try {
            val result = tts?.setLanguage(targetLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.US
            }
        } catch (e: Exception) {
            tts?.language = Locale.US
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "signbridge_tts_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun setRate(rate: Float) {
        _speechRate.value = rate
        tts?.setSpeechRate(rate)
    }

    fun setPitch(pitch: Float) {
        _speechPitch.value = pitch
        tts?.setPitch(pitch)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

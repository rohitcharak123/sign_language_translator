package com.example.audio

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccessibilityFeedbackManager(private val context: Context) {

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _vibrationEnabled.value = enabled
    }

    /**
     * Crisp single tick when a sign gesture is successfully recognized.
     */
    fun vibrateSignRecognized() {
        if (!_vibrationEnabled.value || vibrator?.hasVibrator() != true) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(55, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(55)
        }
    }

    /**
     * Subtle double pulse when spoken speech from hearing partner is detected.
     */
    fun vibrateSpeechDetected() {
        if (!_vibrationEnabled.value || vibrator?.hasVibrator() != true) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 40, 80, 60)
            val amplitudes = intArrayOf(0, 140, 0, 180)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 40, 80, 60), -1)
        }
    }

    /**
     * Distinct high-intensity rhythm for emergency alerts (e.g., "HELP", "DOCTOR", "EMERGENCY")
     */
    fun vibrateUrgentAlert() {
        if (!_vibrationEnabled.value || vibrator?.hasVibrator() != true) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 120, 70, 120, 70, 220)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 120, 70, 120, 70, 220), -1)
        }
    }
}

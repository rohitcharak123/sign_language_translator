package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.SignLanguage
import com.example.ui.SignBridgeViewModel
import com.example.ui.components.CameraVisionView
import com.example.ui.components.TranslationCard
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.GlowAmber

@Composable
fun SignToSpeechScreen(
    viewModel: SignBridgeViewModel,
    modifier: Modifier = Modifier
) {
    val translationState by viewModel.translationState.collectAsStateWithLifecycle()
    val trackedHand by viewModel.visionEngine.trackedHand.collectAsStateWithLifecycle()
    val secondHand by viewModel.visionEngine.trackedSecondHand.collectAsStateWithLifecycle()
    val faceLandmarks by viewModel.visionEngine.trackedFace.collectAsStateWithLifecycle()
    val poseLandmarks by viewModel.visionEngine.trackedPose.collectAsStateWithLifecycle()
    val showMesh by viewModel.visionEngine.showLandmarkMesh.collectAsStateWithLifecycle()
    val activeLang by viewModel.activeLanguage.collectAsStateWithLifecycle()

    val quickSigns = listOf("HELLO", "HELP", "THANK YOU", "WHERE", "WATER", "I AM DEAF", "YES", "NO", "DOCTOR")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Split: Camera Vision View with Real-time Skeletal Tracking Overlay
        CameraVisionView(
            trackedHand = trackedHand,
            secondHand = secondHand,
            faceLandmarks = faceLandmarks,
            poseLandmarks = poseLandmarks,
            emotion = translationState.emotion,
            viseme = translationState.currentViseme,
            showMesh = showMesh,
            onToggleMesh = { viewModel.visionEngine.toggleLandmarkMesh() }
        )

        // Quick Sign Practice / Trigger Strip
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PanTool,
                    contentDescription = null,
                    tint = GlowAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sign Gestures (Tap to simulate or sign in camera):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickSigns.forEach { sign ->
                    FilterChip(
                        selected = translationState.recognizedGlosses.contains(sign),
                        onClick = { viewModel.triggerSignGesture(sign) },
                        label = {
                            Text(
                                text = sign,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricTeal.copy(alpha = 0.25f),
                            selectedLabelColor = ElectricTeal
                        ),
                        modifier = Modifier.testTag("quick_sign_chip_$sign")
                    )
                }
            }
        }

        // Bottom Split: Real-time Translation Card with Natural Grammar and Spoken Audio
        TranslationCard(
            glosses = translationState.recognizedGlosses,
            fluentSentence = translationState.naturalSentence,
            confidence = translationState.confidence,
            datasetSource = activeLang.datasetRef,
            detectedEmotion = translationState.emotion,
            isSpeakingAudio = translationState.isSpeakingAudio,
            isGrammarRefining = translationState.isGrammarRefining,
            onSpeakAudio = { viewModel.speakCurrentTranslation() },
            onClearGlosses = { viewModel.clearGlosses() }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

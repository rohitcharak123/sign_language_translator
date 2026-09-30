package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avatar.SignAvatarRenderer
import com.example.ui.SignBridgeViewModel
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.GlowAmber
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeechToSignScreen(
    viewModel: SignBridgeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val spokenInput by viewModel.spokenInputText.collectAsStateWithLifecycle()
    val avatarGlosses by viewModel.avatarGlosses.collectAsStateWithLifecycle()
    val activeLang by viewModel.activeLanguage.collectAsStateWithLifecycle()

    var isListeningSpeech by remember { mutableStateOf(false) }

    // Speech-to-text system launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListeningSpeech = false
        val spokenWords = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val heard = spokenWords?.firstOrNull()
        if (!heard.isNullOrBlank()) {
            viewModel.setSpokenInput(heard)
            viewModel.translateSpokenTextToSign()
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to translate into ${activeLang.displayName}...")
            }
            try {
                isListeningSpeech = true
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                isListeningSpeech = false
            }
        }
    }

    val presets = listOf(
        "Hello, how are you?",
        "Can I help you?",
        "Where is the doctor?",
        "I need water please",
        "Thank you very much"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Speech Input Bar with Microphone
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "HEARING PERSON SPEECH INPUT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = ElectricTeal,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = spokenInput,
                        onValueChange = { viewModel.setSpokenInput(it) },
                        placeholder = { Text("Speak or type message here...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("speech_text_input"),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = false,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Microphone STT button
                    FilledIconButton(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to translate into ${activeLang.displayName}...")
                                }
                                try {
                                    isListeningSpeech = true
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {
                                    isListeningSpeech = false
                                }
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isListeningSpeech) Color(0xFFEF4444) else ElectricTeal,
                            contentColor = Color(0xFF003641)
                        ),
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("mic_speech_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Speak into microphone"
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Send / Translate button
                    FilledIconButton(
                        onClick = { viewModel.translateSpokenTextToSign() },
                        enabled = spokenInput.isNotBlank(),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = GlowAmber,
                            contentColor = Color(0xFF432C00)
                        ),
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("translate_speech_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Translate to Signs"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Presets row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        FilterChip(
                            selected = spokenInput == preset,
                            onClick = {
                                viewModel.setSpokenInput(preset)
                                viewModel.translateSpokenTextToSign()
                            },
                            label = { Text(preset, fontSize = 12.sp) },
                            modifier = Modifier.testTag("preset_speech_${preset.take(6)}")
                        )
                    }
                }
            }
        }

        // 3D Signer Avatar Canvas View
        SignAvatarRenderer(
            glossSequence = avatarGlosses,
            modifier = Modifier.fillMaxWidth()
        )

        // Grammar & Spatial Structure Breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = GlowAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${activeLang.displayName} Grammar Adaptation:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Structure: ${activeLang.grammarStructure}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Avatar Sign Gloss Sequence:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    avatarGlosses.forEachIndexed { index, gloss ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GlowAmber.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${index + 1}. $gloss",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

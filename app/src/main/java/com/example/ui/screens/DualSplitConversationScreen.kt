package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SignBridgeViewModel
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.GlowAmber

@Composable
fun DualSplitConversationScreen(
    viewModel: SignBridgeViewModel,
    modifier: Modifier = Modifier
) {
    val translationState by viewModel.translationState.collectAsStateWithLifecycle()
    val spokenInput by viewModel.spokenInputText.collectAsStateWithLifecycle()
    val activeLang by viewModel.activeLanguage.collectAsStateWithLifecycle()

    var isFlippedForPartner by remember { mutableStateOf(true) }
    var hearingReplyText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // TOP HALF: Hearing Person Side (Flippable 180° so partner across table can read!)
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .rotate(if (isFlippedForPartner) 180f else 0f)
                .testTag("hearing_partner_pane"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HEARING PARTNER SIDE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { isFlippedForPartner = !isFlippedForPartner },
                        modifier = Modifier.testTag("flip_partner_orientation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenRotation,
                            contentDescription = "Flip Orientation 180°"
                        )
                    }
                }

                // What deaf signer just signed (Spoken translation for hearing partner)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Signer says (${activeLang.code}):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (translationState.naturalSentence.isNotBlank()) {
                                translationState.naturalSentence
                            } else {
                                "Waiting for signer to gesture..."
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Hearing partner text/speech reply field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hearingReplyText,
                        onValueChange = { hearingReplyText = it },
                        placeholder = { Text("Reply to signer...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hearing_reply_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (hearingReplyText.isNotBlank()) {
                                viewModel.setSpokenInput(hearingReplyText)
                                viewModel.translateSpokenTextToSign()
                                hearingReplyText = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal, contentColor = Color(0xFF003641)),
                        modifier = Modifier.testTag("send_reply_to_signer_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Reply")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Center Conversation Divider Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ElectricTeal.copy(alpha = 0.2f),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Text(
                    text = "↔ Two-Way Face-to-Face Dual Bridge",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ElectricTeal,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // BOTTOM HALF: Deaf / Non-Speaking Signer Side
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("deaf_signer_pane"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PanTool,
                            contentDescription = null,
                            tint = GlowAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DEAF / SIGNER SIDE (${activeLang.displayName})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GlowAmber
                        )
                    }

                    // Speak aloud button
                    IconButton(
                        onClick = { viewModel.speakCurrentTranslation() },
                        enabled = translationState.naturalSentence.isNotBlank(),
                        modifier = Modifier.testTag("dual_speak_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak Aloud",
                            tint = ElectricTeal
                        )
                    }
                }

                // What hearing person just said (Displayed in large high-contrast text for deaf person!)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Hearing partner said:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (spokenInput.isNotBlank()) spokenInput else "Listening for hearing partner...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Quick Sign Response Chips for Deaf Person
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickReplies = listOf("YES", "NO", "THANK YOU", "HELP", "I AM DEAF")
                    quickReplies.forEach { reply ->
                        Button(
                            onClick = {
                                viewModel.triggerSignGesture(reply)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(reply, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

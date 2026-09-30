package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SignLanguage
import com.example.ui.theme.AlertCoral
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.GlowAmber

@Composable
fun TopBarBridge(
    activeLanguage: SignLanguage,
    onSelectLanguage: (SignLanguage) -> Unit,
    onTriggerEmergencySos: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSosConfirmation by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & Logo Branding
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ElectricTeal.copy(alpha = 0.18f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "SignBridge Logo",
                        tint = ElectricTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "SignBridge",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Deaf & Hearing Bridge",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Action Buttons: Language Selector Pill & Emergency SOS Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Language selector button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showLanguageDialog = true }
                    .testTag("language_selector_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = activeLanguage.flagEmoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activeLanguage.code,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Sign Language",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Emergency SOS Quick Sign Button
            FilledTonalButton(
                onClick = { showSosConfirmation = true },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = AlertCoral.copy(alpha = 0.2f),
                    contentColor = AlertCoral
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("sos_emergency_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = "Emergency SOS",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SOS",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }

    // Language Selection Modal
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = "Select Sign Language",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                LazyColumn {
                    items(SignLanguage.entries) { lang ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onSelectLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .testTag("select_language_${lang.code}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (lang == activeLanguage) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = lang.flagEmoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${lang.displayName} (${lang.code})",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Dataset: ${lang.datasetRef}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Grammar: ${lang.grammarStructure}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Emergency SOS Confirmation Modal
    if (showSosConfirmation) {
        AlertDialog(
            onDismissRequest = { showSosConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = AlertCoral,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Broadcast Emergency Alert?",
                    fontWeight = FontWeight.Bold,
                    color = AlertCoral
                )
            },
            text = {
                Text(
                    text = "This will immediately flash visual cues, vibrate in emergency cadence, display 'I AM DEAF - NEED IMMEDIATE ASSISTANCE', and speak the urgent help message via speaker.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        showSosConfirmation = false
                        onTriggerEmergencySos()
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AlertCoral,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("confirm_sos_button")
                ) {
                    Text("Broadcast SOS Now")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSosConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

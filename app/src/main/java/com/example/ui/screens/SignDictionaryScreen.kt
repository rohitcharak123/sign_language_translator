package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SignEntity
import com.example.model.NavigationTab
import com.example.ui.SignBridgeViewModel
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.GlowAmber

@Composable
fun SignDictionaryScreen(
    viewModel: SignBridgeViewModel,
    modifier: Modifier = Modifier
) {
    val availableSigns by viewModel.availableSigns.collectAsStateWithLifecycle()
    val customSigns by viewModel.customSigns.collectAsStateWithLifecycle()
    val activeLang by viewModel.activeLanguage.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Emergency", "Greetings", "Basics", "Questions", "Custom")

    // Filter signs based on search and category
    val allSigns = remember(availableSigns, customSigns) {
        val list = mutableListOf<SignEntity>()
        list.addAll(customSigns)
        list.addAll(availableSigns)
        list.distinctBy { it.gloss }
    }

    val filteredSigns = allSigns.filter { sign ->
        val matchesQuery = sign.gloss.contains(searchQuery, ignoreCase = true) ||
                sign.translation.contains(searchQuery, ignoreCase = true)
        val matchesCategory = when (selectedCategory) {
            "All" -> true
            "Custom" -> sign.isCustom
            else -> sign.category.equals(selectedCategory, ignoreCase = true)
        }
        matchesQuery && matchesCategory
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ElectricTeal,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_custom_sign_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Add Custom Sign")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Sign", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search signs in ${activeLang.code}...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dictionary_search_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("category_chip_$cat")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Results count
            Text(
                text = "${filteredSigns.size} Signs available offline",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Signs List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredSigns, key = { "${it.id}_${it.gloss}" }) { sign ->
                    SignItemCard(
                        sign = sign,
                        onPracticeWithAvatar = {
                            viewModel.setSpokenInput(sign.translation)
                            viewModel.translateSpokenTextToSign()
                            viewModel.selectTab(NavigationTab.SPEECH_TO_SIGN)
                        },
                        onPracticeGesture = {
                            viewModel.triggerSignGesture(sign.gloss)
                            viewModel.selectTab(NavigationTab.SIGN_TO_SPEECH)
                        },
                        onDeleteCustom = if (sign.isCustom) { { viewModel.deleteCustomSign(sign.id) } } else null
                    )
                }
            }
        }
    }

    // Add Custom Sign Dialog
    if (showAddDialog) {
        var newGloss by remember { mutableStateOf("") }
        var newTranslation by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf("Basics") }
        var newDescription by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("Personalize Custom Sign", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Teach SignBridge your custom gestures, personal signs, or regional family variations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newGloss,
                        onValueChange = { newGloss = it },
                        label = { Text("Sign Gloss (e.g. MOM, COFFEE)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newTranslation,
                        onValueChange = { newTranslation = it },
                        label = { Text("Spoken Translation") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Handshape / Movement Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGloss.isNotBlank() && newTranslation.isNotBlank()) {
                            viewModel.saveCustomSign(
                                gloss = newGloss,
                                translation = newTranslation,
                                category = newCategory,
                                description = newDescription
                            )
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_custom_sign_confirm_button")
                ) {
                    Text("Save Sign")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SignItemCard(
    sign: SignEntity,
    onPracticeWithAvatar: () -> Unit,
    onPracticeGesture: () -> Unit,
    onDeleteCustom: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sign.gloss,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (sign.isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GlowAmber.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Personalized",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = sign.datasetSource,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = sign.translation,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = ElectricTeal
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = sign.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (sign.handShapeHint.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tip: ${sign.handShapeHint}",
                    style = MaterialTheme.typography.labelSmall,
                    color = GlowAmber
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons (Avatar Demo & Gesture Practice)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onPracticeWithAvatar,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("avatar_demo_${sign.gloss}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("3D Avatar", fontSize = 12.sp)
                    }

                    FilledTonalButton(
                        onClick = onPracticeGesture,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("practice_gesture_${sign.gloss}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PanTool,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Sign", fontSize = 12.sp)
                    }
                }

                if (onDeleteCustom != null) {
                    IconButton(onClick = onDeleteCustom) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Custom Sign",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

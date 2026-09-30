package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.EmotionType
import com.example.model.NavigationTab
import com.example.ui.components.TopBarBridge
import com.example.ui.screens.DualSplitConversationScreen
import com.example.ui.screens.PrivacySettingsScreen
import com.example.ui.screens.SignDictionaryScreen
import com.example.ui.screens.SignToSpeechScreen
import com.example.ui.screens.SpeechToSignScreen
import com.example.ui.theme.AlertCoral
import com.example.ui.theme.ElectricTeal

@Composable
fun MainScreen(
    viewModel: SignBridgeViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeLang by viewModel.activeLanguage.collectAsStateWithLifecycle()
    val translationState by viewModel.translationState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column {
                TopBarBridge(
                    activeLanguage = activeLang,
                    onSelectLanguage = { viewModel.setLanguage(it) },
                    onTriggerEmergencySos = { viewModel.triggerEmergencySos() }
                )

                // High-visibility Emergency Alert Banner when SOS triggered
                if (translationState.emotion == EmotionType.URGENCY && translationState.recognizedGlosses.contains("HELP")) {
                    Surface(
                        color = AlertCoral,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EMERGENCY ALERT BROADCAST ACTIVE: I AM DEAF",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        icon = {
                            val icon = when (tab) {
                                NavigationTab.SIGN_TO_SPEECH -> Icons.Default.PanTool
                                NavigationTab.SPEECH_TO_SIGN -> Icons.Default.Hearing
                                NavigationTab.DUAL_CONVERSATION -> Icons.AutoMirrored.Filled.CompareArrows
                                NavigationTab.DICTIONARY -> Icons.Default.Book
                                NavigationTab.SETTINGS -> Icons.Default.Security
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricTeal,
                            selectedTextColor = ElectricTeal,
                            indicatorColor = ElectricTeal.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { tab ->
                when (tab) {
                    NavigationTab.SIGN_TO_SPEECH -> SignToSpeechScreen(viewModel = viewModel)
                    NavigationTab.SPEECH_TO_SIGN -> SpeechToSignScreen(viewModel = viewModel)
                    NavigationTab.DUAL_CONVERSATION -> DualSplitConversationScreen(viewModel = viewModel)
                    NavigationTab.DICTIONARY -> SignDictionaryScreen(viewModel = viewModel)
                    NavigationTab.SETTINGS -> PrivacySettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

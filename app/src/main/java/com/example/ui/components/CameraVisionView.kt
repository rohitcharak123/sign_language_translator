package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.EmotionType
import com.example.model.FaceLandmarks
import com.example.model.HandLandmarks
import com.example.model.PoseLandmarks
import com.example.model.VisemeType
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.GlowAmber

@Composable
fun CameraVisionView(
    trackedHand: HandLandmarks,
    secondHand: HandLandmarks?,
    faceLandmarks: FaceLandmarks?,
    poseLandmarks: PoseLandmarks?,
    emotion: EmotionType,
    viseme: VisemeType,
    showMesh: Boolean,
    onToggleMesh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isFrontCamera by remember { mutableStateOf(true) }
    var useSimulationFeed by remember { mutableStateOf(!hasCameraPermission) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) useSimulationFeed = false
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("camera_vision_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            // Camera Feed or High-Fidelity Silhouette Feed
            if (hasCameraPermission && !useSimulationFeed) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }
                                val cameraSelector = if (isFrontCamera) {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                }
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                            } catch (exc: Exception) {
                                Log.e("CameraVisionView", "Camera binding failed", exc)
                                useSimulationFeed = true
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    }
                )
            } else {
                // Interactive Silhouette / Privacy Mode Studio Feed
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E293B)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (!hasCameraPermission) {
                            Text(
                                text = "Camera permission needed for live tracking",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                modifier = Modifier.testTag("grant_camera_permission_button")
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enable Camera")
                            }
                        } else {
                            Text(
                                text = "Simulated Gesture Track Feed",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricTeal
                            )
                            Text(
                                text = "Camera feed paused (Privacy Mode Active)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }

            // Real-time skeletal overlay canvas
            if (showMesh) {
                LandmarkOverlayCanvas(
                    hand = trackedHand,
                    secondHand = secondHand,
                    face = faceLandmarks,
                    pose = poseLandmarks,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Top Status Bar Overlay (Emotion, Viseme, Live Indicator)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left badge: Emotion & Non-manual marker
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = emotion.emoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = emotion.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Center badge: Lip reading viseme indicator
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.75f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Lip Reading",
                            tint = GlowAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Lip: ${viseme.label}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlowAmber
                        )
                    }
                }

                // Right: On-device privacy indicator
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF047857).copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "ON-DEVICE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Bottom action bar controls (Toggle mesh, flip camera, simulation switch)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Toggle skeleton mesh
                FilledTonalIconButton(
                    onClick = onToggleMesh,
                    modifier = Modifier.testTag("toggle_landmark_mesh_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.75f),
                        contentColor = if (showMesh) ElectricTeal else Color.Gray
                    )
                ) {
                    Icon(
                        imageVector = if (showMesh) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Landmark Mesh"
                    )
                }

                // Switch front/back camera
                if (hasCameraPermission && !useSimulationFeed) {
                    FilledTonalIconButton(
                        onClick = { isFrontCamera = !isFrontCamera },
                        modifier = Modifier.testTag("flip_camera_button"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.75f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Flip Camera"
                        )
                    }
                }

                // Switch between camera & simulation feed
                FilledTonalIconButton(
                    onClick = {
                        if (!hasCameraPermission) {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            useSimulationFeed = !useSimulationFeed
                        }
                    },
                    modifier = Modifier.testTag("toggle_simulation_feed_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.75f),
                        contentColor = if (useSimulationFeed) GlowAmber else Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (useSimulationFeed) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        contentDescription = "Toggle Camera/Simulation"
                    )
                }
            }
        }
    }
}

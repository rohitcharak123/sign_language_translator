package com.example.avatar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VisemeType
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.GlowAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SignAvatarRenderer(
    glossSequence: List<String>,
    modifier: Modifier = Modifier,
    avatarSkinColor: Color = Color(0xFFFFD4B2),
    avatarShirtColor: Color = Color(0xFF1E3A8A),
    onSequenceFinished: () -> Unit = {}
) {
    var speedMultiplier by remember { mutableFloatStateOf(1.0f) }
    var currentGlossIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }

    val activeGloss = if (glossSequence.isNotEmpty() && currentGlossIndex in glossSequence.indices) {
        glossSequence[currentGlossIndex]
    } else {
        "REST"
    }

    val keyframes = remember(activeGloss) {
        AvatarSignLibrary.getSignKeyframes(activeGloss)
    }

    var keyframeIndex by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Playback loop
    LaunchedEffect(glossSequence, speedMultiplier) {
        if (glossSequence.isNotEmpty()) {
            isPlaying = true
            currentGlossIndex = 0
            for (gIdx in glossSequence.indices) {
                currentGlossIndex = gIdx
                val frames = AvatarSignLibrary.getSignKeyframes(glossSequence[gIdx])
                for (kIdx in 0 until frames.size - 1) {
                    keyframeIndex = kIdx
                    progress.snapTo(0f)
                    val frameDuration = (650 / speedMultiplier).toInt()
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = frameDuration, easing = LinearEasing)
                    )
                }
                delay((300 / speedMultiplier).toLong())
            }
            isPlaying = false
            onSequenceFinished()
        }
    }

    val currentPose = keyframes.getOrElse(keyframeIndex) { AvatarSignLibrary.REST_POSE }
    val nextPose = keyframes.getOrElse((keyframeIndex + 1) % keyframes.size) { AvatarSignLibrary.REST_POSE }

    // Interpolate pose values based on animatable progress
    val t = progress.value
    val rightElbow = Offset(
        x = lerp(currentPose.rightElbowX, nextPose.rightElbowX, t),
        y = lerp(currentPose.rightElbowY, nextPose.rightElbowY, t)
    )
    val rightWrist = Offset(
        x = lerp(currentPose.rightWristX, nextPose.rightWristX, t),
        y = lerp(currentPose.rightWristY, nextPose.rightWristY, t)
    )
    val leftElbow = Offset(
        x = lerp(currentPose.leftElbowX, nextPose.leftElbowX, t),
        y = lerp(currentPose.leftElbowY, nextPose.leftElbowY, t)
    )
    val leftWrist = Offset(
        x = lerp(currentPose.leftWristX, nextPose.leftWristX, t),
        y = lerp(currentPose.leftWristY, nextPose.leftWristY, t)
    )
    val smile = lerp(currentPose.smileCurve, nextPose.smileCurve, t)
    val eyebrow = lerp(currentPose.eyebrowLift, nextPose.eyebrowLift, t)
    val activeViseme = if (t > 0.5f) nextPose.viseme else currentPose.viseme
    val headTilt = lerp(currentPose.headTiltAngle, nextPose.headTiltAngle, t)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("avatar_renderer_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Active sign gloss badge & Viseme indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ElectricTeal.copy(alpha = 0.2f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3D SIGN AVATAR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElectricTeal
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) GlowAmber else Color.Gray)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "Mouth Viseme: ${activeViseme.label}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Canvas Avatar View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
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
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawAvatar(
                        w = size.width,
                        h = size.height,
                        headTilt = headTilt,
                        eyebrowLift = eyebrow,
                        smileCurve = smile,
                        viseme = activeViseme,
                        rightElbow = rightElbow,
                        rightWrist = rightWrist,
                        leftElbow = leftElbow,
                        leftWrist = leftWrist,
                        skinColor = avatarSkinColor,
                        shirtColor = avatarShirtColor
                    )
                }

                // Gloss subtitle chip
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.75f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Signing: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                        Text(
                            text = activeGloss,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = GlowAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Playback controls (Speed toggle, Replay)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Playback Speed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Speed: ${speedMultiplier}x",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = {
                            speedMultiplier = when (speedMultiplier) {
                                0.5f -> 1.0f
                                1.0f -> 1.5f
                                else -> 0.5f
                            }
                        },
                        modifier = Modifier.testTag("avatar_speed_button")
                    ) {
                        Text("${speedMultiplier}x")
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isPlaying = true
                                for (kIdx in 0 until keyframes.size - 1) {
                                    keyframeIndex = kIdx
                                    progress.snapTo(0f)
                                    progress.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = (650 / speedMultiplier).toInt(),
                                            easing = LinearEasing
                                        )
                                    )
                                }
                                isPlaying = false
                            }
                        },
                        modifier = Modifier.testTag("avatar_replay_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Replay Sign"
                        )
                    }
                }
            }
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

private fun DrawScope.drawAvatar(
    w: Float,
    h: Float,
    headTilt: Float,
    eyebrowLift: Float,
    smileCurve: Float,
    viseme: VisemeType,
    rightElbow: Offset,
    rightWrist: Offset,
    leftElbow: Offset,
    leftWrist: Offset,
    skinColor: Color,
    shirtColor: Color
) {
    val centerX = w * 0.5f
    val headRadius = w * 0.12f
    val headCenterY = h * 0.28f

    // 1. Shoulders & Torso
    val leftShoulder = Offset(w * 0.34f, h * 0.44f)
    val rightShoulder = Offset(w * 0.66f, h * 0.44f)

    val torsoPath = Path().apply {
        moveTo(leftShoulder.x, leftShoulder.y)
        lineTo(rightShoulder.x, rightShoulder.y)
        lineTo(w * 0.72f, h * 0.95f)
        lineTo(w * 0.28f, h * 0.95f)
        close()
    }
    drawPath(torsoPath, color = shirtColor)

    // Neck
    drawRect(
        color = skinColor,
        topLeft = Offset(centerX - w * 0.04f, headCenterY + headRadius * 0.7f),
        size = Size(w * 0.08f, h * 0.10f)
    )

    // 2. Arms (Shoulder -> Elbow -> Wrist)
    val limbStroke = w * 0.055f
    val rElbowPx = Offset(rightElbow.x * w, rightElbow.y * h)
    val rWristPx = Offset(rightWrist.x * w, rightWrist.y * h)
    val lElbowPx = Offset(leftElbow.x * w, leftElbow.y * h)
    val lWristPx = Offset(leftWrist.x * w, leftWrist.y * h)

    // Right Arm (Dominant)
    drawLine(
        color = shirtColor,
        start = rightShoulder,
        end = rElbowPx,
        strokeWidth = limbStroke,
        cap = StrokeCap.Round
    )
    drawLine(
        color = skinColor,
        start = rElbowPx,
        end = rWristPx,
        strokeWidth = limbStroke * 0.85f,
        cap = StrokeCap.Round
    )
    // Right Hand (Palm & Fingers)
    drawCircle(
        color = skinColor,
        radius = w * 0.045f,
        center = rWristPx
    )

    // Left Arm (Non-dominant)
    drawLine(
        color = shirtColor,
        start = leftShoulder,
        end = lElbowPx,
        strokeWidth = limbStroke,
        cap = StrokeCap.Round
    )
    drawLine(
        color = skinColor,
        start = lElbowPx,
        end = lWristPx,
        strokeWidth = limbStroke * 0.85f,
        cap = StrokeCap.Round
    )
    // Left Hand
    drawCircle(
        color = skinColor,
        radius = w * 0.045f,
        center = lWristPx
    )

    // 3. Head (with subtle 3D lighting shading)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(skinColor, skinColor.copy(red = skinColor.red * 0.85f)),
            center = Offset(centerX, headCenterY),
            radius = headRadius
        ),
        radius = headRadius,
        center = Offset(centerX, headCenterY)
    )

    // Hair
    val hairPath = Path().apply {
        moveTo(centerX - headRadius * 1.05f, headCenterY - headRadius * 0.1f)
        cubicTo(
            centerX - headRadius, headCenterY - headRadius * 1.25f,
            centerX + headRadius, headCenterY - headRadius * 1.25f,
            centerX + headRadius * 1.05f, headCenterY - headRadius * 0.1f
        )
        lineTo(centerX + headRadius * 0.85f, headCenterY - headRadius * 0.5f)
        lineTo(centerX - headRadius * 0.85f, headCenterY - headRadius * 0.5f)
        close()
    }
    drawPath(hairPath, color = Color(0xFF1E293B))

    // Eyes
    val eyeY = headCenterY - headRadius * 0.08f
    val eyeOffsetX = headRadius * 0.35f
    drawCircle(color = Color(0xFF0F172A), radius = w * 0.015f, center = Offset(centerX - eyeOffsetX, eyeY))
    drawCircle(color = Color(0xFF0F172A), radius = w * 0.015f, center = Offset(centerX + eyeOffsetX, eyeY))
    // Eye shine
    drawCircle(color = Color.White, radius = w * 0.005f, center = Offset(centerX - eyeOffsetX + 2, eyeY - 2))
    drawCircle(color = Color.White, radius = w * 0.005f, center = Offset(centerX + eyeOffsetX + 2, eyeY - 2))

    // Eyebrows (Dynamic lift based on questions/expressions)
    val browY = eyeY - headRadius * (0.20f + (eyebrowLift - 0.5f) * 0.22f)
    val browStroke = w * 0.012f
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(centerX - eyeOffsetX - headRadius * 0.2f, browY),
        end = Offset(centerX - eyeOffsetX + headRadius * 0.2f, browY),
        strokeWidth = browStroke,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(centerX + eyeOffsetX - headRadius * 0.2f, browY),
        end = Offset(centerX + eyeOffsetX + headRadius * 0.2f, browY),
        strokeWidth = browStroke,
        cap = StrokeCap.Round
    )

    // Nose
    drawLine(
        color = Color(0xFF9E7762),
        start = Offset(centerX, eyeY + headRadius * 0.1f),
        end = Offset(centerX, eyeY + headRadius * 0.32f),
        strokeWidth = w * 0.009f,
        cap = StrokeCap.Round
    )

    // Mouth / Dynamic Viseme Lip sync!
    val mouthY = headCenterY + headRadius * 0.50f
    val mouthWidth = headRadius * 0.50f

    when (viseme) {
        VisemeType.OPEN_AO -> {
            // Oval open vowel mouth
            drawOval(
                color = Color(0xFF7F1D1D),
                topLeft = Offset(centerX - mouthWidth * 0.35f, mouthY - headRadius * 0.12f),
                size = Size(mouthWidth * 0.70f, headRadius * 0.26f)
            )
        }
        VisemeType.ROUNDED_W -> {
            // Small round mouth
            drawCircle(
                color = Color(0xFF7F1D1D),
                radius = mouthWidth * 0.22f,
                center = Offset(centerX, mouthY)
            )
        }
        VisemeType.LABIODENTAL_FV -> {
            // Teeth visible on lower lip
            drawOval(
                color = Color(0xFF7F1D1D),
                topLeft = Offset(centerX - mouthWidth * 0.45f, mouthY - headRadius * 0.05f),
                size = Size(mouthWidth * 0.90f, headRadius * 0.12f)
            )
            // Teeth line
            drawLine(
                color = Color.White,
                start = Offset(centerX - mouthWidth * 0.30f, mouthY - headRadius * 0.02f),
                end = Offset(centerX + mouthWidth * 0.30f, mouthY - headRadius * 0.02f),
                strokeWidth = w * 0.008f
            )
        }
        VisemeType.BILABIAL_MBP, VisemeType.REST -> {
            // Closed / gentle curve
            val mouthPath = Path().apply {
                moveTo(centerX - mouthWidth * 0.45f, mouthY)
                quadraticTo(
                    centerX, mouthY + headRadius * (smileCurve * 0.18f),
                    centerX + mouthWidth * 0.45f, mouthY
                )
            }
            drawPath(mouthPath, color = Color(0xFF7F1D1D), style = Stroke(width = w * 0.014f, cap = StrokeCap.Round))
        }
        VisemeType.DENTAL_TH -> {
            // Tongue visible
            drawOval(
                color = Color(0xFF7F1D1D),
                topLeft = Offset(centerX - mouthWidth * 0.4f, mouthY - headRadius * 0.06f),
                size = Size(mouthWidth * 0.8f, headRadius * 0.14f)
            )
            drawCircle(
                color = Color(0xFFF472B6),
                radius = w * 0.012f,
                center = Offset(centerX, mouthY)
            )
        }
    }
}

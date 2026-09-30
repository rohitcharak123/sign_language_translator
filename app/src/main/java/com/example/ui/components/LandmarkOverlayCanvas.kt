package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.FaceLandmarks
import com.example.model.HandLandmarks
import com.example.model.PoseLandmarks
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.GlowAmber
import com.example.ui.theme.LandmarkFace
import com.example.ui.theme.LandmarkFinger
import com.example.ui.theme.LandmarkPose
import com.example.ui.theme.LandmarkWrist

/**
 * Draws real-time MediaPipe skeletal landmarks (Hands, Face mesh, Pose)
 * with high-visibility neon colors for accessibility feedback.
 */
@Composable
fun LandmarkOverlayCanvas(
    hand: HandLandmarks,
    secondHand: HandLandmarks? = null,
    face: FaceLandmarks? = null,
    pose: PoseLandmarks? = null,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Draw Pose Landmarks (Shoulders to Elbows to Wrists)
        if (pose != null && pose.isPresent) {
            val lShoulder = Offset(pose.leftShoulder.x * w, pose.leftShoulder.y * h)
            val rShoulder = Offset(pose.rightShoulder.x * w, pose.rightShoulder.y * h)
            val lElbow = Offset(pose.leftElbow.x * w, pose.leftElbow.y * h)
            val rElbow = Offset(pose.rightElbow.x * w, pose.rightElbow.y * h)

            // Shoulder bar
            drawLine(LandmarkPose, lShoulder, rShoulder, strokeWidth = 5f, cap = StrokeCap.Round)
            // Left arm
            drawLine(LandmarkPose, lShoulder, lElbow, strokeWidth = 4f, cap = StrokeCap.Round)
            // Right arm
            drawLine(LandmarkPose, rShoulder, rElbow, strokeWidth = 4f, cap = StrokeCap.Round)

            drawCircle(Color.White, radius = 6f, center = lShoulder)
            drawCircle(Color.White, radius = 6f, center = rShoulder)
            drawCircle(Color.White, radius = 5f, center = lElbow)
            drawCircle(Color.White, radius = 5f, center = rElbow)
        }

        // 2. Draw Face Mesh Points (Lip contour & Eyebrow arc)
        if (face != null && face.isPresent) {
            val nose = Offset(face.noseTip.x * w, face.noseTip.y * h)
            drawCircle(LandmarkFace, radius = 5f, center = nose)

            // Eyebrow indicators
            val eyeLevelY = (face.noseTip.y - 0.08f - (face.eyebrowHeightRatio - 0.5f) * 0.06f) * h
            val leftBrow = Offset(w * 0.42f, eyeLevelY)
            val rightBrow = Offset(w * 0.58f, eyeLevelY)
            drawLine(ElectricTeal, Offset(w * 0.38f, eyeLevelY), Offset(w * 0.46f, eyeLevelY), strokeWidth = 4f, cap = StrokeCap.Round)
            drawLine(ElectricTeal, Offset(w * 0.54f, eyeLevelY), Offset(w * 0.62f, eyeLevelY), strokeWidth = 4f, cap = StrokeCap.Round)

            // Lip contour
            val mouthY = (face.noseTip.y + 0.09f) * h
            val mouthWidth = w * (0.08f + face.mouthWidth * 0.04f)
            val mouthHeight = h * (0.02f + face.mouthAperture * 0.05f)

            drawOval(
                color = GlowAmber,
                topLeft = Offset((face.noseTip.x * w) - mouthWidth * 0.5f, mouthY - mouthHeight * 0.5f),
                size = androidx.compose.ui.geometry.Size(mouthWidth, mouthHeight),
                style = Stroke(width = 3f)
            )
        }

        // 3. Draw Hand 21-point MediaPipe Skeletal Bones
        drawHandBones(hand, w, h, isPrimary = true)
        if (secondHand != null && secondHand.isPresent) {
            drawHandBones(secondHand, w, h, isPrimary = false)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHandBones(
    hand: HandLandmarks,
    w: Float,
    h: Float,
    isPrimary: Boolean
) {
    if (!hand.isPresent || hand.points.size < 21) return

    val pts = hand.points.map { Offset(it.x * w, it.y * h) }
    val boneColor = if (isPrimary) LandmarkFinger else GlowAmber
    val jointColor = Color.White
    val strokeW = 4f

    // Bone chains (MediaPipe standard fingers topology)
    val fingerChains = listOf(
        listOf(0, 1, 2, 3, 4),       // Thumb
        listOf(0, 5, 6, 7, 8),       // Index
        listOf(0, 9, 10, 11, 12),    // Middle
        listOf(0, 13, 14, 15, 16),   // Ring
        listOf(0, 17, 18, 19, 20)    // Pinky
    )

    // Palm bridge between MCP knuckles
    val palmKnuckles = listOf(5, 9, 13, 17)
    for (i in 0 until palmKnuckles.size - 1) {
        drawLine(
            color = boneColor.copy(alpha = 0.8f),
            start = pts[palmKnuckles[i]],
            end = pts[palmKnuckles[i + 1]],
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }

    // Finger lines
    for (chain in fingerChains) {
        for (i in 0 until chain.size - 1) {
            drawLine(
                color = boneColor,
                start = pts[chain[i]],
                end = pts[chain[i + 1]],
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }
    }

    // Joint markers
    pts.forEachIndexed { index, pt ->
        val radius = when (index) {
            0 -> 8f // Wrist
            4, 8, 12, 16, 20 -> 6f // Fingertips
            else -> 4f
        }
        val ptColor = if (index == 0) LandmarkWrist else jointColor
        drawCircle(color = ptColor, radius = radius, center = pt)
    }
}

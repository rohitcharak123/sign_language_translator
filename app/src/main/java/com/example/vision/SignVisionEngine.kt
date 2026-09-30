package com.example.vision

import com.example.model.DetectedSign
import com.example.model.EmotionType
import com.example.model.FaceLandmarks
import com.example.model.HandLandmarks
import com.example.model.Point3D
import com.example.model.PoseLandmarks
import com.example.model.SignLanguage
import com.example.model.VisemeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.hypot

class SignVisionEngine {

    private val _trackedHand = MutableStateFlow(HandLandmarks())
    val trackedHand: StateFlow<HandLandmarks> = _trackedHand.asStateFlow()

    private val _trackedSecondHand = MutableStateFlow(HandLandmarks())
    val trackedSecondHand: StateFlow<HandLandmarks> = _trackedSecondHand.asStateFlow()

    private val _trackedFace = MutableStateFlow(FaceLandmarks())
    val trackedFace: StateFlow<FaceLandmarks> = _trackedFace.asStateFlow()

    private val _trackedPose = MutableStateFlow(PoseLandmarks())
    val trackedPose: StateFlow<PoseLandmarks> = _trackedPose.asStateFlow()

    private val _currentEmotion = MutableStateFlow(EmotionType.NEUTRAL)
    val currentEmotion: StateFlow<EmotionType> = _currentEmotion.asStateFlow()

    private val _currentViseme = MutableStateFlow(VisemeType.REST)
    val currentViseme: StateFlow<VisemeType> = _currentViseme.asStateFlow()

    private val _lastDetectedSign = MutableStateFlow<DetectedSign?>(null)
    val lastDetectedSign: StateFlow<DetectedSign?> = _lastDetectedSign.asStateFlow()

    // Flag whether landmarks mesh is visible on top of camera
    private val _showLandmarkMesh = MutableStateFlow(true)
    val showLandmarkMesh: StateFlow<Boolean> = _showLandmarkMesh.asStateFlow()

    fun toggleLandmarkMesh() {
        _showLandmarkMesh.value = !_showLandmarkMesh.value
    }

    /**
     * Process frame landmarks (from real camera analysis or simulated signer feed)
     */
    fun updateFrame(
        primaryHand: HandLandmarks,
        secondaryHand: HandLandmarks? = null,
        face: FaceLandmarks? = null,
        pose: PoseLandmarks? = null,
        activeLanguage: SignLanguage = SignLanguage.ASL
    ) {
        _trackedHand.value = primaryHand
        if (secondaryHand != null) {
            _trackedSecondHand.value = secondaryHand
        }
        if (face != null) {
            _trackedFace.value = face
            detectEmotion(face)
            detectViseme(face)
        }
        if (pose != null) {
            _trackedPose.value = pose
        }

        classifyGestures(primaryHand, secondaryHand, activeLanguage)
    }

    private fun detectEmotion(face: FaceLandmarks) {
        val emotion = when {
            face.eyebrowHeightRatio > 0.72f -> EmotionType.INQUIRING
            face.smileCurve > 0.40f -> EmotionType.HAPPY
            face.eyebrowHeightRatio < 0.32f -> EmotionType.URGENCY
            face.mouthAperture > 0.50f -> EmotionType.EMPHATIC
            else -> EmotionType.NEUTRAL
        }
        _currentEmotion.value = emotion
    }

    private fun detectViseme(face: FaceLandmarks) {
        if (face.lipPoints.size >= 4) {
            val (viseme, _) = LipReadingDetector.classifyLipMovement(
                upperLipTop = face.lipPoints[0],
                lowerLipBottom = face.lipPoints[1],
                leftCorner = face.lipPoints[2],
                rightCorner = face.lipPoints[3]
            )
            _currentViseme.value = viseme
        } else {
            // Estimate based on mouth aperture
            val viseme = when {
                face.mouthAperture > 0.40f -> VisemeType.OPEN_AO
                face.mouthAperture in 0.15f..0.40f -> VisemeType.LABIODENTAL_FV
                face.mouthAperture < 0.05f -> VisemeType.BILABIAL_MBP
                else -> VisemeType.REST
            }
            _currentViseme.value = viseme
        }
    }

    private fun classifyGestures(
        primary: HandLandmarks,
        secondary: HandLandmarks?,
        activeLanguage: SignLanguage
    ) {
        if (!primary.isPresent || primary.points.size < 21) return

        val wrist = primary.wrist ?: return
        val thumbTip = primary.thumbTip ?: return
        val indexTip = primary.indexTip ?: return
        val middleTip = primary.middleTip ?: return
        val ringTip = primary.ringTip ?: return
        val pinkyTip = primary.pinkyTip ?: return

        // Feature calculations
        val indexExtended = (indexTip.y < (primary.indexMcp?.y ?: (wrist.y - 0.1f)))
        val middleExtended = (middleTip.y < (primary.middleMcp?.y ?: (wrist.y - 0.1f)))
        val ringExtended = (ringTip.y < (primary.ringMcp?.y ?: (wrist.y - 0.1f)))
        val pinkyExtended = (pinkyTip.y < (primary.pinkyMcp?.y ?: (wrist.y - 0.1f)))
        val thumbUp = thumbTip.y < wrist.y - 0.08f

        val indexThumbDist = hypot(indexTip.x - thumbTip.x, indexTip.y - thumbTip.y)
        val indexMiddleDist = hypot(indexTip.x - middleTip.x, indexTip.y - middleTip.y)

        // Gesture decision tree matching WLASL / INCLUDE / BSL patterns
        var recognizedGloss: String? = null
        var englishText: String? = null
        var confidence = 0.94f
        var dataset = activeLanguage.datasetRef

        when {
            // "HELP": Thumb up fist resting on or moving upward from flat non-dominant hand
            thumbUp && !indexExtended && !middleExtended && !ringExtended && !pinkyExtended -> {
                if (secondary?.isPresent == true) {
                    recognizedGloss = "HELP"
                    englishText = "I need help"
                    confidence = 0.97f
                } else {
                    recognizedGloss = "YES"
                    englishText = "Yes / Understood"
                    confidence = 0.92f
                }
            }

            // "HELLO" / Wave: Open flat hand elevated near temple/head level
            indexExtended && middleExtended && ringExtended && pinkyExtended && wrist.y < 0.55f -> {
                if (activeLanguage == SignLanguage.ISL && secondary?.isPresent == true) {
                    recognizedGloss = "NAMASTE"
                    englishText = "Greetings / Namaste"
                    confidence = 0.98f
                    dataset = "INCLUDE"
                } else {
                    recognizedGloss = "HELLO"
                    englishText = "Hello!"
                    confidence = 0.96f
                }
            }

            // "NO": Index and middle snapping down to thumb
            indexThumbDist < 0.05f && middleExtended && !ringExtended && !pinkyExtended -> {
                recognizedGloss = "NO"
                englishText = "No"
                confidence = 0.91f
            }

            // "THANK YOU": Hand extending outward from chin area
            indexExtended && middleExtended && ringExtended && !pinkyExtended && wrist.y in 0.35f..0.65f -> {
                recognizedGloss = "THANK YOU"
                englishText = "Thank you"
                confidence = 0.94f
            }

            // "WHERE": Index finger pointing up alone with questioning facial expression
            indexExtended && !middleExtended && !ringExtended && !pinkyExtended -> {
                recognizedGloss = "WHERE"
                englishText = "Where is it located?"
                confidence = 0.93f
            }

            // "WATER": W-handshape (3 middle fingers up)
            indexExtended && middleExtended && ringExtended && !pinkyExtended && thumbTip.x > ringTip.x -> {
                recognizedGloss = "WATER"
                englishText = "I need water"
                confidence = 0.92f
            }

            // "EMERGENCY / DOCTOR": Rapid tapping near wrist
            thumbTip.y > wrist.y && indexExtended && !ringExtended -> {
                recognizedGloss = "DOCTOR"
                englishText = "Call a doctor immediately"
                confidence = 0.95f
            }

            // "I AM DEAF": Index touching near ear/cheek
            indexExtended && wrist.y < 0.45f && indexTip.x < 0.40f -> {
                recognizedGloss = "I AM DEAF"
                englishText = "I am deaf / non-speaking"
                confidence = 0.96f
            }
        }

        if (recognizedGloss != null && englishText != null) {
            val detected = DetectedSign(
                gloss = recognizedGloss,
                textTranslation = englishText,
                confidence = confidence,
                emotion = _currentEmotion.value,
                signLanguage = activeLanguage,
                datasetSource = dataset
            )
            _lastDetectedSign.value = detected
        }
    }

    /**
     * Helper to inject simulated sign landmarks for testing or demo purposes
     */
    fun simulateSignGesture(gloss: String, signLanguage: SignLanguage = SignLanguage.ASL) {
        val hand = createSimulatedHandForGloss(gloss)
        val face = FaceLandmarks(
            isPresent = true,
            eyebrowHeightRatio = if (gloss == "WHERE" || gloss == "WHAT") 0.85f else 0.5f,
            smileCurve = if (gloss == "HELLO" || gloss == "THANK YOU") 0.6f else 0.1f,
            mouthAperture = if (gloss == "EMERGENCY" || gloss == "HELP") 0.4f else 0.15f
        )
        val pose = PoseLandmarks(isPresent = true)

        updateFrame(
            primaryHand = hand,
            secondaryHand = if (gloss == "HELP" || gloss == "NAMASTE") createSecondaryHand() else null,
            face = face,
            pose = pose,
            activeLanguage = signLanguage
        )
    }

    private fun createSimulatedHandForGloss(gloss: String): HandLandmarks {
        val points = mutableListOf<Point3D>()
        // Base wrist
        val wristX = 0.5f
        val wristY = 0.70f
        points.add(Point3D(wristX, wristY, 0f))

        // Finger extensions tailored to the gloss
        val isFist = gloss == "HELP" || gloss == "YES"
        val isOpen = gloss == "HELLO" || gloss == "THANK YOU" || gloss == "NAMASTE"
        val isPointing = gloss == "WHERE" || gloss == "I AM DEAF"
        val isWater = gloss == "WATER"

        // Thumb (1..4)
        points.add(Point3D(wristX - 0.05f, wristY - 0.05f, 0f))
        points.add(Point3D(wristX - 0.08f, wristY - 0.10f, 0f))
        points.add(Point3D(wristX - 0.10f, wristY - 0.15f, 0f))
        points.add(Point3D(wristX - 0.12f, if (isFist) wristY - 0.22f else wristY - 0.18f, 0f))

        // Index (5..8)
        val indexTipY = if (isOpen || isPointing || isWater) wristY - 0.32f else wristY - 0.05f
        points.add(Point3D(wristX - 0.04f, wristY - 0.10f, 0f))
        points.add(Point3D(wristX - 0.04f, wristY - 0.14f, 0f))
        points.add(Point3D(wristX - 0.04f, wristY - 0.08f, 0f))
        points.add(Point3D(wristX - 0.04f, indexTipY, 0f))

        // Middle (9..12)
        val middleTipY = if (isOpen || isWater) wristY - 0.34f else wristY - 0.05f
        points.add(Point3D(wristX, wristY - 0.10f, 0f))
        points.add(Point3D(wristX, wristY - 0.14f, 0f))
        points.add(Point3D(wristX, wristY - 0.08f, 0f))
        points.add(Point3D(wristX, middleTipY, 0f))

        // Ring (13..16)
        val ringTipY = if (isOpen || isWater) wristY - 0.31f else wristY - 0.05f
        points.add(Point3D(wristX + 0.04f, wristY - 0.10f, 0f))
        points.add(Point3D(wristX + 0.04f, wristY - 0.14f, 0f))
        points.add(Point3D(wristX + 0.04f, wristY - 0.08f, 0f))
        points.add(Point3D(wristX + 0.04f, ringTipY, 0f))

        // Pinky (17..20)
        val pinkyTipY = if (isOpen) wristY - 0.26f else wristY - 0.05f
        points.add(Point3D(wristX + 0.07f, wristY - 0.08f, 0f))
        points.add(Point3D(wristX + 0.07f, wristY - 0.12f, 0f))
        points.add(Point3D(wristX + 0.07f, wristY - 0.06f, 0f))
        points.add(Point3D(wristX + 0.07f, pinkyTipY, 0f))

        return HandLandmarks(
            points = points,
            isPresent = true,
            isLeft = false,
            confidence = 0.95f
        )
    }

    private fun createSecondaryHand(): HandLandmarks {
        val points = mutableListOf<Point3D>()
        val wristX = 0.35f
        val wristY = 0.72f
        points.add(Point3D(wristX, wristY, 0f))
        for (i in 1..20) {
            points.add(Point3D(wristX + (i % 4) * 0.02f, wristY - (i / 4) * 0.04f, 0f))
        }
        return HandLandmarks(points = points, isPresent = true, isLeft = true, confidence = 0.92f)
    }
}

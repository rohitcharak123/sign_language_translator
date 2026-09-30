package com.example.model

data class Point3D(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
)

/**
 * 21 landmarks standard in MediaPipe Hands
 */
data class HandLandmarks(
    val points: List<Point3D> = emptyList(),
    val isPresent: Boolean = false,
    val isLeft: Boolean = false,
    val confidence: Float = 0f
) {
    val wrist: Point3D? get() = points.getOrNull(0)
    val thumbTip: Point3D? get() = points.getOrNull(4)
    val indexTip: Point3D? get() = points.getOrNull(8)
    val middleTip: Point3D? get() = points.getOrNull(12)
    val ringTip: Point3D? get() = points.getOrNull(16)
    val pinkyTip: Point3D? get() = points.getOrNull(20)

    val indexMcp: Point3D? get() = points.getOrNull(5)
    val middleMcp: Point3D? get() = points.getOrNull(9)
    val ringMcp: Point3D? get() = points.getOrNull(13)
    val pinkyMcp: Point3D? get() = points.getOrNull(17)
}

data class FaceLandmarks(
    val isPresent: Boolean = false,
    val eyebrowHeightRatio: Float = 0.5f,
    val smileCurve: Float = 0f,
    val mouthAperture: Float = 0f,
    val mouthWidth: Float = 0.5f,
    val headTilt: Float = 0f,
    val noseTip: Point3D = Point3D(0.5f, 0.35f, 0f),
    val lipPoints: List<Point3D> = emptyList()
)

data class PoseLandmarks(
    val isPresent: Boolean = false,
    val leftShoulder: Point3D = Point3D(0.35f, 0.55f, 0f),
    val rightShoulder: Point3D = Point3D(0.65f, 0.55f, 0f),
    val leftElbow: Point3D = Point3D(0.28f, 0.75f, 0f),
    val rightElbow: Point3D = Point3D(0.72f, 0.75f, 0f),
    val leftWrist: Point3D = Point3D(0.32f, 0.90f, 0f),
    val rightWrist: Point3D = Point3D(0.68f, 0.90f, 0f)
)

enum class VisemeType(val label: String, val phoneticExample: String) {
    REST("Rest", "Silence / Neutral mouth"),
    BILABIAL_MBP("M / B / P", "Lips pressed together"),
    LABIODENTAL_FV("F / V", "Upper teeth on lower lip"),
    OPEN_AO("A / O", "Mouth wide open / Vowel"),
    DENTAL_TH("TH / L", "Tongue between teeth"),
    ROUNDED_W("W / OO", "Lips puckered / rounded")
}

enum class EmotionType(val displayName: String, val emoji: String, val toneDescription: String) {
    NEUTRAL("Neutral", "😐", "Standard affirmative statement"),
    INQUIRING("Question / Inquiring", "❓", "Raised eyebrows, question syntax"),
    HAPPY("Friendly / Happy", "😊", "Warm smile, positive engagement"),
    URGENCY("Urgent / Emergency", "🚨", "High intensity, immediate attention"),
    EMPHATIC("Emphatic", "❗", "Strong focal stress, deliberate signing")
}

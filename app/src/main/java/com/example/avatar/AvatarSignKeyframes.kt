package com.example.avatar

import com.example.model.VisemeType

/**
 * 3D Avatar Keyframe Pose containing arm joints, hand shape, and facial expression
 */
data class AvatarPose(
    val label: String,
    // Right arm (dominant)
    val rightElbowX: Float = 0.65f,
    val rightElbowY: Float = 0.55f,
    val rightWristX: Float = 0.60f,
    val rightWristY: Float = 0.40f,
    val rightHandSpread: Float = 0.8f, // 0 = fist, 1 = open hand
    val rightIndexPoint: Boolean = false,
    val rightThumbUp: Boolean = false,

    // Left arm (non-dominant)
    val leftElbowX: Float = 0.35f,
    val leftElbowY: Float = 0.55f,
    val leftWristX: Float = 0.40f,
    val leftWristY: Float = 0.40f,
    val leftHandSpread: Float = 0.8f,
    val isTwoHanded: Boolean = false,

    // Head & Face
    val headTiltAngle: Float = 0f, // degrees
    val eyebrowLift: Float = 0.5f,  // 0 = furrowed, 1 = raised
    val smileCurve: Float = 0.3f,   // 0 = neutral/sad, 1 = broad smile
    val viseme: VisemeType = VisemeType.REST
)

object AvatarSignLibrary {

    val REST_POSE = AvatarPose(
        label = "Rest",
        rightElbowX = 0.70f,
        rightElbowY = 0.62f,
        rightWristX = 0.62f,
        rightWristY = 0.75f,
        rightHandSpread = 0.3f,
        leftElbowX = 0.30f,
        leftElbowY = 0.62f,
        leftWristX = 0.38f,
        leftWristY = 0.75f,
        leftHandSpread = 0.3f,
        isTwoHanded = false,
        headTiltAngle = 0f,
        eyebrowLift = 0.5f,
        smileCurve = 0.2f,
        viseme = VisemeType.REST
    )

    fun getSignKeyframes(gloss: String): List<AvatarPose> {
        val normalized = gloss.trim().uppercase()
        return when (normalized) {
            "HELLO", "HI" -> listOf(
                REST_POSE,
                AvatarPose(
                    label = "Salute prep",
                    rightElbowX = 0.72f,
                    rightElbowY = 0.40f,
                    rightWristX = 0.60f,
                    rightWristY = 0.25f,
                    rightHandSpread = 1.0f,
                    eyebrowLift = 0.8f,
                    smileCurve = 0.7f,
                    viseme = VisemeType.LABIODENTAL_FV
                ),
                AvatarPose(
                    label = "Wave outward",
                    rightElbowX = 0.75f,
                    rightElbowY = 0.38f,
                    rightWristX = 0.75f,
                    rightWristY = 0.22f,
                    rightHandSpread = 1.0f,
                    eyebrowLift = 0.8f,
                    smileCurve = 0.9f,
                    viseme = VisemeType.OPEN_AO
                ),
                REST_POSE
            )

            "THANK YOU", "THANKS" -> listOf(
                REST_POSE,
                AvatarPose(
                    label = "Touch chin",
                    rightElbowX = 0.65f,
                    rightElbowY = 0.48f,
                    rightWristX = 0.52f,
                    rightWristY = 0.33f,
                    rightHandSpread = 1.0f,
                    eyebrowLift = 0.6f,
                    smileCurve = 0.6f,
                    viseme = VisemeType.DENTAL_TH
                ),
                AvatarPose(
                    label = "Extend forward",
                    rightElbowX = 0.65f,
                    rightElbowY = 0.50f,
                    rightWristX = 0.52f,
                    rightWristY = 0.42f,
                    rightHandSpread = 1.0f,
                    eyebrowLift = 0.7f,
                    smileCurve = 0.8f,
                    viseme = VisemeType.ROUNDED_W
                ),
                REST_POSE
            )

            "HELP" -> listOf(
                REST_POSE,
                AvatarPose(
                    label = "Fist on palm base",
                    rightElbowX = 0.62f,
                    rightElbowY = 0.55f,
                    rightWristX = 0.50f,
                    rightWristY = 0.52f,
                    rightHandSpread = 0f,
                    rightThumbUp = true,
                    leftElbowX = 0.38f,
                    leftElbowY = 0.58f,
                    leftWristX = 0.50f,
                    leftWristY = 0.54f,
                    leftHandSpread = 1.0f,
                    isTwoHanded = true,
                    eyebrowLift = 0.85f,
                    viseme = VisemeType.LABIODENTAL_FV
                ),
                AvatarPose(
                    label = "Lift together",
                    rightElbowX = 0.62f,
                    rightElbowY = 0.48f,
                    rightWristX = 0.50f,
                    rightWristY = 0.40f,
                    rightHandSpread = 0f,
                    rightThumbUp = true,
                    leftElbowX = 0.38f,
                    leftElbowY = 0.50f,
                    leftWristX = 0.50f,
                    leftWristY = 0.42f,
                    leftHandSpread = 1.0f,
                    isTwoHanded = true,
                    eyebrowLift = 0.9f,
                    viseme = VisemeType.OPEN_AO
                ),
                REST_POSE
            )

            "YES" -> listOf(
                REST_POSE,
                AvatarPose(
                    label = "Fist up",
                    rightElbowX = 0.68f,
                    rightElbowY = 0.50f,
                    rightWristX = 0.60f,
                    rightWristY = 0.38f,
                    rightHandSpread = 0f,
                    headTiltAngle = 5f,
                    viseme = VisemeType.OPEN_AO
                ),
                AvatarPose(
                    label = "Fist nod down",
                    rightElbowX = 0.68f,
                    rightElbowY = 0.52f,
                    rightWristX = 0.60f,
                    rightWristY = 0.45f,
                    rightHandSpread = 0f,
                    headTiltAngle = 10f,
                    viseme = VisemeType.REST
                ),
                REST_POSE
            )

            "NO" -> listOf(
                REST_POSE,
                AvatarPose(
                    label = "Fingers open snap",
                    rightElbowX = 0.68f,
                    rightElbowY = 0.48f,
                    rightWristX = 0.58f,
                    rightWristY = 0.38f,
                    rightHandSpread = 0.8f,
                    headTiltAngle = -8f,
                    eyebrowLift = 0.3f,
                    viseme = VisemeType.BILABIAL_MBP
                ),
                AvatarPose(
                    label = "Snap fingers closed",
                    rightElbowX = 0.68f,
                    rightElbowY = 0.50f,
                    rightWristX = 0.58f,
                    rightWristY = 0.40f,
                    rightHandSpread = 0.1f,
                    headTiltAngle = 8f,
                    eyebrowLift = 0.25f,
                    viseme = VisemeType.ROUNDED_W
                ),
                REST_POSE
            )

            "WHERE", "WHERE?" -> listOf(
                REST_POSE,
                AvatarPose(
                    label = "Index wag left",
                    rightElbowX = 0.68f,
                    rightElbowY = 0.48f,
                    rightWristX = 0.55f,
                    rightWristY = 0.35f,
                    rightIndexPoint = true,
                    headTiltAngle = 6f,
                    eyebrowLift = 0.2f, // Furrowed for WH-questions
                    viseme = VisemeType.ROUNDED_W
                ),
                AvatarPose(
                    label = "Index wag right",
                    rightElbowX = 0.68f,
                    rightElbowY = 0.48f,
                    rightWristX = 0.63f,
                    rightWristY = 0.35f,
                    rightIndexPoint = true,
                    headTiltAngle = -6f,
                    eyebrowLift = 0.2f,
                    viseme = VisemeType.OPEN_AO
                ),
                REST_POSE
            )

            "PLEASE" -> listOf(
                REST_POSE,
                AvatarPose(
                    label = "Palm on chest",
                    rightElbowX = 0.66f,
                    rightElbowY = 0.54f,
                    rightWristX = 0.50f,
                    rightWristY = 0.48f,
                    rightHandSpread = 1.0f,
                    eyebrowLift = 0.7f,
                    smileCurve = 0.5f,
                    viseme = VisemeType.BILABIAL_MBP
                ),
                AvatarPose(
                    label = "Circle on chest",
                    rightElbowX = 0.66f,
                    rightElbowY = 0.52f,
                    rightWristX = 0.52f,
                    rightWristY = 0.44f,
                    rightHandSpread = 1.0f,
                    eyebrowLift = 0.75f,
                    smileCurve = 0.6f,
                    viseme = VisemeType.OPEN_AO
                ),
                REST_POSE
            )

            "I AM DEAF", "DEAF" -> listOf(
                REST_POSE,
                AvatarPose(
                    label = "Touch ear",
                    rightElbowX = 0.72f,
                    rightElbowY = 0.45f,
                    rightWristX = 0.58f,
                    rightWristY = 0.26f,
                    rightIndexPoint = true,
                    eyebrowLift = 0.6f,
                    viseme = VisemeType.DENTAL_TH
                ),
                AvatarPose(
                    label = "Touch mouth",
                    rightElbowX = 0.70f,
                    rightElbowY = 0.48f,
                    rightWristX = 0.53f,
                    rightWristY = 0.33f,
                    rightIndexPoint = true,
                    eyebrowLift = 0.6f,
                    viseme = VisemeType.LABIODENTAL_FV
                ),
                REST_POSE
            )

            else -> listOf(
                // Generic sign / fingerspelling fallback
                REST_POSE,
                AvatarPose(
                    label = "Expressive sign",
                    rightElbowX = 0.68f,
                    rightElbowY = 0.48f,
                    rightWristX = 0.55f,
                    rightWristY = 0.38f,
                    rightHandSpread = 0.7f,
                    eyebrowLift = 0.6f,
                    viseme = VisemeType.OPEN_AO
                ),
                REST_POSE
            )
        }
    }
}

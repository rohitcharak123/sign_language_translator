package com.example.vision

import com.example.model.Point3D
import com.example.model.VisemeType
import kotlin.math.hypot

object LipReadingDetector {

    /**
     * Computes mouth aspect ratio and classifies into standard visemes
     * based on vertical lip aperture and horizontal lip spread.
     */
    fun classifyLipMovement(
        upperLipTop: Point3D,
        lowerLipBottom: Point3D,
        leftCorner: Point3D,
        rightCorner: Point3D
    ): Pair<VisemeType, Float> {
        val verticalDist = hypot(lowerLipBottom.x - upperLipTop.x, lowerLipBottom.y - upperLipTop.y)
        val horizontalDist = hypot(rightCorner.x - leftCorner.x, rightCorner.y - leftCorner.y)

        if (horizontalDist < 0.001f) {
            return Pair(VisemeType.REST, 0.5f)
        }

        val mouthAspectRatio = verticalDist / horizontalDist

        return when {
            mouthAspectRatio > 0.45f -> {
                // Wide open vertical mouth
                Pair(VisemeType.OPEN_AO, (mouthAspectRatio * 1.5f).coerceAtMost(1f))
            }
            mouthAspectRatio in 0.22f..0.45f -> {
                // Rounded / puckered or dental
                if (horizontalDist < 0.25f) {
                    Pair(VisemeType.ROUNDED_W, 0.85f)
                } else {
                    Pair(VisemeType.DENTAL_TH, 0.80f)
                }
            }
            mouthAspectRatio in 0.08f..0.22f -> {
                Pair(VisemeType.LABIODENTAL_FV, 0.82f)
            }
            mouthAspectRatio < 0.08f && verticalDist < 0.03f -> {
                // Lips firmly pressed together
                Pair(VisemeType.BILABIAL_MBP, 0.90f)
            }
            else -> {
                Pair(VisemeType.REST, 0.70f)
            }
        }
    }
}

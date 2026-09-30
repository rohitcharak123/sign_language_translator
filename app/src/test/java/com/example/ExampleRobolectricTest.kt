package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.Point3D
import com.example.model.SignLanguage
import com.example.model.VisemeType
import com.example.vision.LipReadingDetector
import com.example.vision.SignVisionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SignBridge", appName)
    }

    @Test
    fun testLipReadingDetectorVisemes() {
        // Wide open mouth -> OPEN_AO
        val (openViseme, conf) = LipReadingDetector.classifyLipMovement(
            upperLipTop = Point3D(0.5f, 0.40f),
            lowerLipBottom = Point3D(0.5f, 0.65f),
            leftCorner = Point3D(0.35f, 0.52f),
            rightCorner = Point3D(0.65f, 0.52f)
        )
        assertEquals(VisemeType.OPEN_AO, openViseme)
        assertTrue(conf > 0.5f)

        // Closed lips pressed together -> BILABIAL_MBP
        val (closedViseme, _) = LipReadingDetector.classifyLipMovement(
            upperLipTop = Point3D(0.5f, 0.51f),
            lowerLipBottom = Point3D(0.5f, 0.52f),
            leftCorner = Point3D(0.35f, 0.515f),
            rightCorner = Point3D(0.65f, 0.515f)
        )
        assertEquals(VisemeType.BILABIAL_MBP, closedViseme)
    }

    @Test
    fun testSignVisionEngineSimulation() {
        val engine = SignVisionEngine()
        engine.simulateSignGesture("HELP", SignLanguage.ASL)

        val lastSign = engine.lastDetectedSign.value
        assertNotNull(lastSign)
        assertEquals("HELP", lastSign?.gloss)
    }
}

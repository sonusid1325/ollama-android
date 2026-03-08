package com.sonusid.ollama.util

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SpriteAnalysisTest {

    @Test
    fun `sobel gradient is detected on vertical edge`() {
        val bitmap = Bitmap.createBitmap(3, 3, Bitmap.Config.ARGB_8888)
        repeat(3) { y ->
            bitmap.setPixel(0, y, Color.BLACK)
            bitmap.setPixel(1, y, Color.BLACK)
            bitmap.setPixel(2, y, Color.WHITE)
        }

        val roi = SpriteAnalysis.prepareGradientRoi(bitmap, centerX = 1, centerY = 1, roiWidth = 3, roiHeight = 3)

        assertNotNull(roi)
        val centerIndex = 1 * 3 + 1
        assertEquals(1020f, roi!!.magnitude[centerIndex], 0.5f)
    }

    @Test
    fun `thresholding and transparency affect match rate`() {
        val reference = createVerticalEdgeBitmap()
        val identical = createVerticalEdgeBitmap()
        val masked = createVerticalEdgeBitmap(makeTransparentRight = true)

        val refRoi = SpriteAnalysis.prepareGradientRoi(reference, centerX = 1, centerY = 1, roiWidth = 3, roiHeight = 3)!!
        val identicalRoi = SpriteAnalysis.prepareGradientRoi(identical, centerX = 1, centerY = 1, roiWidth = 3, roiHeight = 3)!!
        val maskedRoi = SpriteAnalysis.prepareGradientRoi(masked, centerX = 1, centerY = 1, roiWidth = 3, roiHeight = 3)!!

        val highScore = SpriteAnalysis.calculateMatchRate(refRoi, identicalRoi, threshold = 500f, useIoU = true)
        val suppressedScore = SpriteAnalysis.calculateMatchRate(refRoi, identicalRoi, threshold = 1500f, useIoU = true)
        val maskedScore = SpriteAnalysis.calculateMatchRate(refRoi, maskedRoi, threshold = 500f, useIoU = true)

        assertEquals(1f, highScore, 0.0001f)
        assertEquals(0f, suppressedScore, 0.0001f)
        assertEquals(0f, maskedScore, 0.0001f)
    }

    @Test
    fun `search finds shifted edge within radius`() = runBlocking {
        val base = Bitmap.createBitmap(4, 3, Bitmap.Config.ARGB_8888)
        val shifted = Bitmap.createBitmap(4, 3, Bitmap.Config.ARGB_8888)
        repeat(3) { y ->
            base.setPixel(0, y, Color.BLACK)
            base.setPixel(1, y, Color.BLACK)
            base.setPixel(2, y, Color.WHITE)
            base.setPixel(3, y, Color.WHITE)

            shifted.setPixel(0, y, Color.BLACK)
            shifted.setPixel(1, y, Color.BLACK)
            shifted.setPixel(2, y, Color.BLACK)
            shifted.setPixel(3, y, Color.WHITE)
        }

        val match = SpriteAnalysis.searchBestOffset(
            reference = base,
            target = shifted,
            centerX = 1,
            centerY = 1,
            roiWidth = 3,
            roiHeight = 3,
            searchRadius = 1,
            threshold = 500f,
            useIoU = false,
        )

        assertNotNull(match)
        assertEquals(1, match!!.offset.dx)
        assertEquals(0, match.offset.dy)
        // max-based の指標でも 1 に近い値になることを確認
        assertEquals(1f, match.score, 0.0001f)
    }

    private fun createVerticalEdgeBitmap(makeTransparentRight: Boolean = false): Bitmap {
        val bitmap = Bitmap.createBitmap(3, 3, Bitmap.Config.ARGB_8888)
        repeat(3) { y ->
            bitmap.setPixel(0, y, Color.BLACK)
            bitmap.setPixel(1, y, Color.BLACK)
            val rightColor = if (makeTransparentRight) Color.TRANSPARENT else Color.WHITE
            bitmap.setPixel(2, y, rightColor)
        }
        return bitmap
    }
}

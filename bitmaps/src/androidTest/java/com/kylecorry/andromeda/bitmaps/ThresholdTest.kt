package com.kylecorry.andromeda.bitmaps

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ThresholdTest {


    @Test
    fun threshold() {
        val zeros = createBitmap()
        val black = createBitmap()
        val ones = createBitmap()
        val expectedOnesBinary = createBitmap()
        val twoValues = createBitmap()
        val expectedTwoValuesBinary = createBitmap()
        val expectedTwoValuesNonBinary = createBitmap()
        val large = createBitmap()
        val largeYellow = createBitmap()

        for (i in 0 until zeros.width) {
            for (j in 0 until zeros.height) {
                zeros.setPixel(i, j, 0)
                black.setPixel(i, j, Color.rgb(0, 0, 0))
                ones.setPixel(i, j, Color.rgb(1, 1, 1))
                expectedOnesBinary.setPixel(i, j, Color.rgb(255, 255, 255))

                if (j < zeros.height / 2) {
                    twoValues.setPixel(i, j, Color.rgb(1, 2, 3))
                    expectedTwoValuesBinary.setPixel(i, j, Color.rgb(0, 0, 0))
                    expectedTwoValuesNonBinary.setPixel(i, j, Color.rgb(0, 0, 0))
                } else {
                    twoValues.setPixel(i, j, Color.rgb(4, 5, 6))
                    expectedTwoValuesBinary.setPixel(i, j, Color.rgb(255, 255, 255))
                    expectedTwoValuesNonBinary.setPixel(i, j, Color.rgb(4, 5, 6))
                }
            }
        }

        large.setPixel(0, 0, Color.rgb(200, 200, 200))
        largeYellow.setPixel(0, 0, Color.rgb(0, 200, 200))

        bitmapEquals(expectedOnesBinary, Toolkit.threshold(ones, 0f, true, channel = 4))
        bitmapEquals(ones, Toolkit.threshold(ones, 0f, false, channel = 4))
        bitmapEquals(black, Toolkit.threshold(ones, 1f, true, channel = 4))
        bitmapEquals(black, Toolkit.threshold(ones, 1f, false, channel = 4))
        bitmapEquals(expectedTwoValuesBinary, Toolkit.threshold(twoValues, 4f, true, channel = 4))
        bitmapEquals(
            expectedTwoValuesNonBinary,
            Toolkit.threshold(twoValues, 4f, false, channel = 4)
        )
        bitmapEquals(large, Toolkit.threshold(large, 180f, false, channel = 4))
        bitmapEquals(largeYellow, Toolkit.threshold(large, 200f, false, channel = 0))

        val inPlaceTwoValues = Toolkit.threshold(twoValues, 4f, true, channel = 4, inPlace = true)
        bitmapEquals(expectedTwoValuesBinary, inPlaceTwoValues)
        assertEquals(inPlaceTwoValues, twoValues)
    }

    private fun bitmapEquals(bitmap1: Bitmap, bitmap2: Bitmap) {
        assertEquals(bitmap1.width, bitmap2.width)
        assertEquals(bitmap1.height, bitmap2.height)

        for (i in 0 until bitmap1.width) {
            for (j in 0 until bitmap1.height) {
                assertEquals(bitmap1.getPixel(i, j), bitmap2.getPixel(i, j))
            }
        }
    }

    private fun createBitmap(width: Int = 100, height: Int = 100): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }
}
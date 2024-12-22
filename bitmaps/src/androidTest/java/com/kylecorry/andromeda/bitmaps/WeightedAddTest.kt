package com.kylecorry.andromeda.bitmaps

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class WeightedAddTest {


    @Test
    fun weightedAdd() {
        val ones = createBitmap()
        val expected1 = createBitmap()
        val expected2 = createBitmap()
        val expected3 = createBitmap()

        for (i in 0 until ones.width) {
            for (j in 0 until ones.height) {
                ones.setPixel(i, j, Color.rgb(1, 1, 1))
                expected1.setPixel(i, j, Color.rgb(2, 2, 2))
                expected2.setPixel(i, j, Color.argb(0, 0, 0, 0))
                expected3.setPixel(i, j, Color.rgb(2, 2, 2))
            }
        }

        val result1 = Toolkit.weightedAdd(ones, ones, 1f, 1f, false)
        val result2 = Toolkit.weightedAdd(ones, ones, 1f, -3f, false)
        val result3 = Toolkit.weightedAdd(ones, ones, 1f, -3f, true)

        bitmapEquals(expected1, result1)
        bitmapEquals(expected2, result2)
        bitmapEquals(expected3, result3)
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
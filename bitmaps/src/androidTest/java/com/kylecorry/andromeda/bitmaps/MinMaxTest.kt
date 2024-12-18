package com.kylecorry.andromeda.bitmaps

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class MinMaxTest {


    @Test
    fun minMax(){
        val zeros = createBitmap()
        val ones = createBitmap()
        val twoValues = createBitmap()

        for (i in 0 until zeros.width){
            for (j in 0 until zeros.height){
                zeros.setPixel(i, j, 0)
                ones.setPixel(i, j, Color.rgb(1, 1, 1))
            }
        }

        twoValues.setPixel(0, 0, Color.rgb(1, 2, 3))
        twoValues.setPixel(1, 0, Color.rgb(4, 5, 6))

        val zerosMinMax = Toolkit.minMax(zeros, channel = 4)
        val onesMinMax = Toolkit.minMax(ones, channel = 4)
        val onesRedMinMax = Toolkit.minMax(ones, channel = 0)
        val twoValuesMinMax = Toolkit.minMax(twoValues, channel = 4)
        val twoValuesRedMinMax = Toolkit.minMax(twoValues, channel = 0)
        val twoValuesGreenMinMax = Toolkit.minMax(twoValues, channel = 1)
        val twoValuesBlueMinMax = Toolkit.minMax(twoValues, channel = 2)

        assertEquals(0f, zerosMinMax[0], 0.01f)
        assertEquals(0f, zerosMinMax[1], 0.01f)
        assertEquals(1f, onesMinMax[0], 0.01f)
        assertEquals(1f, onesMinMax[1], 0.01f)
        assertEquals(1f, onesRedMinMax[0], 0.01f)
        assertEquals(1f, onesRedMinMax[1], 0.01f)
        assertEquals(0f, twoValuesMinMax[0], 0.01f)
        assertEquals(5f, twoValuesMinMax[1], 0.01f)
        assertEquals(0f, twoValuesRedMinMax[0], 0.01f)
        assertEquals(4f, twoValuesRedMinMax[1], 0.01f)
        assertEquals(0f, twoValuesGreenMinMax[0], 0.01f)
        assertEquals(5f, twoValuesGreenMinMax[1], 0.01f)
        assertEquals(0f, twoValuesBlueMinMax[0], 0.01f)
        assertEquals(6f, twoValuesBlueMinMax[1], 0.01f)
    }

    private fun createBitmap(width: Int = 100, height: Int = 100): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }
}
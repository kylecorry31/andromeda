package com.kylecorry.andromeda.bitmaps

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class AverageTest {


    @Test
    fun average(){
        val zeros = createBitmap()
        val ones = createBitmap()
        val twoValues = createBitmap()

        for (i in 0 until zeros.width){
            for (j in 0 until zeros.height){
                zeros.setPixel(i, j, 0)
                ones.setPixel(i, j, Color.rgb(1, 1, 1))

                if (j < zeros.height / 2){
                    twoValues.setPixel(i, j, Color.rgb(1, 2, 3))
                } else {
                    twoValues.setPixel(i, j, Color.rgb(4, 5, 6))
                }
            }
        }

        twoValues.setPixel(0, 0, Color.rgb(1, 2, 3))
        twoValues.setPixel(1, 0, Color.rgb(4, 5, 6))

        val zerosAverage = Toolkit.average(zeros, channel = 4)
        val onesAverage = Toolkit.average(ones, channel = 4)
        val onesRedAverage = Toolkit.average(ones, channel = 0)
        val twoValuesAverage = Toolkit.average(twoValues, channel = 4)
        val twoValuesRedAverage = Toolkit.average(twoValues, channel = 0)
        val twoValuesGreenAverage = Toolkit.average(twoValues, channel = 1)
        val twoValuesBlueAverage = Toolkit.average(twoValues, channel = 2)

        assertEquals(0.0, zerosAverage, 0.01)
        assertEquals(1.0, onesAverage, 0.01)
        assertEquals(1.0, onesRedAverage, 0.01)
        assertEquals(3.5, twoValuesAverage, 0.01)
        assertEquals(2.5, twoValuesRedAverage, 0.01)
        assertEquals(3.5, twoValuesGreenAverage, 0.01)
        assertEquals(4.5, twoValuesBlueAverage, 0.01)
    }

    private fun createBitmap(width: Int = 100, height: Int = 100): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }
}
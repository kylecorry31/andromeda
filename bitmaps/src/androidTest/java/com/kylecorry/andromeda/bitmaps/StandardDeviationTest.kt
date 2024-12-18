package com.kylecorry.andromeda.bitmaps

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class StandardDeviationTest {


    @Test
    fun standardDeviation(){
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

        val zerosStandardDeviation = Toolkit.standardDeviation(zeros, channel = 4)
        val onesStandardDeviation = Toolkit.standardDeviation(ones, channel = 4)
        val onesRedStandardDeviation = Toolkit.standardDeviation(ones, channel = 0)
        val twoValuesStandardDeviation = Toolkit.standardDeviation(twoValues, channel = 4)
        val twoValuesRedStandardDeviation = Toolkit.standardDeviation(twoValues, channel = 0)
        val twoValuesGreenStandardDeviation = Toolkit.standardDeviation(twoValues, channel = 1)
        val twoValuesBlueStandardDeviation = Toolkit.standardDeviation(twoValues, channel = 2)

        assertEquals(0.0, zerosStandardDeviation, 0.01)
        assertEquals(0.0, onesStandardDeviation, 0.01)
        assertEquals(0.0, onesRedStandardDeviation, 0.01)
        assertEquals(1.5, twoValuesStandardDeviation, 0.01)
        assertEquals(1.5, twoValuesRedStandardDeviation, 0.01)
        assertEquals(1.5, twoValuesGreenStandardDeviation, 0.01)
        assertEquals(1.5, twoValuesBlueStandardDeviation, 0.01)
    }

    private fun createBitmap(width: Int = 100, height: Int = 100): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }
}
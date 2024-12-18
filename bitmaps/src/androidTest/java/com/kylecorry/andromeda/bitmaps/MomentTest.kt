package com.kylecorry.andromeda.bitmaps

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class MomentTest {


    @Test
    fun moment(){
        val zeros = createBitmap()
        val ones = createBitmap()

        for (i in ones.width / 4 until 3 * ones.width / 4){
            for (j in 2 * ones.height / 5 until ones.height){
                ones.setPixel(i, j, Color.rgb(1, 1, 1))
            }
        }

        val zerosMoment = Toolkit.moment(zeros, channel = 4)
        val onesMoment = Toolkit.moment(ones, channel = 4)

        assertEquals(0f, zerosMoment[0], 0.01f)
        assertEquals(0f, zerosMoment[1], 0.01f)
        assertEquals((ones.width / 2).toFloat() - 0.5f, onesMoment[0], 0.01f)
        assertEquals(3.5f * ones.height / 5f - 0.5f, onesMoment[1], 0.01f)
    }

    private fun createBitmap(width: Int = 100, height: Int = 100): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }
}
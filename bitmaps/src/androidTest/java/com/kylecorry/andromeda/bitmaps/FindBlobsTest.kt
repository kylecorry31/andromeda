package com.kylecorry.andromeda.bitmaps

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class FindBlobsTest {


    @Test
    fun findBlobs(){
        val zeros = createBitmap()
        val same = createBitmap()
        val blobs2 = createBitmap()

        // Fill same with white
        for (x in 0 until same.width){
            for (y in 0 until same.height){
                same.setPixel(x, y, Color.WHITE)
            }
        }

        // 5x5 blob
        for (x in 0 until 5){
            for (y in 0 until 5){
                blobs2.setPixel(x, y, Color.WHITE)
            }
        }

        // 2x2 blob
        for (x in 0 until 2){
            for (y in 0 until 2){
                blobs2.setPixel(x + 10, y + 10, Color.WHITE)
            }
        }

        val zerosBlobs = Toolkit.findBlobs(zeros, 4, 127f, 10)
        val onesBlobs = Toolkit.findBlobs(blobs2, 4, 127f, 10)
        val sameBlobs = Toolkit.findBlobs(same, 4, 127f, 10)

        assertEquals(0, zerosBlobs.size)
        assertEquals(2, onesBlobs.size)
        assertEquals(25, onesBlobs[0].width() * onesBlobs[0].height())
        assertEquals(4, onesBlobs[1].width() * onesBlobs[1].height())
        assertEquals(1, sameBlobs.size)
        assertEquals(10000, sameBlobs[0].width() * sameBlobs[0].height())
    }

    private fun createBitmap(width: Int = 100, height: Int = 100): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }
}
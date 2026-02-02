package com.kylecorry.andromeda.bitmaps.operations

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import com.kylecorry.andromeda.bitmaps.operations.BitmapOperation
import kotlin.also

class Dither(private val destinationConfig: Bitmap.Config? = null) : BitmapOperation {

    private val paint = Paint().also {
        it.isDither = true
    }

    override fun execute(bitmap: Bitmap): Bitmap {
        val newBitmap = createBitmap(
            bitmap.width,
            bitmap.height,
            destinationConfig ?: bitmap.config ?: Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(newBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return newBitmap
    }
}
package com.kylecorry.andromeda.bitmaps.operations

import android.graphics.Bitmap

class NoOp : BitmapOperation {
    override fun execute(bitmap: Bitmap): Bitmap {
        return bitmap
    }
}
package com.kylecorry.andromeda.bitmaps.operations

import android.graphics.Bitmap

interface BitmapOperation {
    fun execute(bitmap: Bitmap): Bitmap
}
package com.kylecorry.andromeda.bitmaps

import android.graphics.Bitmap
import android.os.Build
import java.io.OutputStream

class ImageSaver(private val losslessWhenQualityIsMax: Boolean = true) {

    fun saveAsWebp(
        image: Bitmap,
        stream: OutputStream,
        quality: Int = 90,
        losslessQuality: Int = 50
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (quality == 100 && losslessWhenQualityIsMax) {
                image.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, losslessQuality, stream)
            } else {
                image.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, stream)
            }
        } else {
            @Suppress("DEPRECATION")
            image.compress(Bitmap.CompressFormat.WEBP, quality, stream)
        }
    }

}

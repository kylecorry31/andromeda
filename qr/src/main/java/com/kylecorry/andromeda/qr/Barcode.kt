package com.kylecorry.andromeda.qr

import android.graphics.Bitmap
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.oned.MultiFormatUPCEANReader

object Barcode {

    fun decode(image: Bitmap, highAccuracy: Boolean = false): String? {
        val pixels = IntArray(image.width * image.height)
        image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
        val binaryBitmap =
            BinaryBitmap(HybridBinarizer(RGBLuminanceSource(image.width, image.height, pixels)))
        return try {
            val hints = if (highAccuracy) {
                mapOf(DecodeHintType.TRY_HARDER to true)
            } else {
                null
            }
            val result = MultiFormatUPCEANReader(null).decode(binaryBitmap, hints)
            result.text
        } catch (e: Exception) {
            null
        }
    }
}
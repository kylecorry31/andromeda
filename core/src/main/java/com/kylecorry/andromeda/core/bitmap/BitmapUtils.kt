package com.kylecorry.andromeda.core.bitmap

import android.content.Context
import android.graphics.*
import android.media.Image
import com.google.android.renderscript.Toolkit
import com.google.android.renderscript.YuvFormat
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

object BitmapUtils {

    fun decodeBitmapScaled(
        path: String,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap {
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, this)
            inSampleSize = calculateInSampleSize(this, maxWidth, maxHeight)
            inJustDecodeBounds = false
            BitmapFactory.decodeFile(path, this)
        }
    }

    fun getBitmapSize(path: String): Pair<Int, Int> {
        val opts = BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, this)
            this
        }
        return Pair(opts.outWidth, opts.outHeight)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun Image.toBitmap(rotation: Float = 90f): Bitmap {
        val yuvBuffer = YuvByteBuffer(this, null)
        val bytes = ByteArray(yuvBuffer.buffer.capacity())
        yuvBuffer.buffer.get(bytes)
        val bmp = Toolkit.yuvToRgbBitmap(bytes, width, height, YuvFormat.NV21)
        return if (rotation != 0f) {
            val rotated = bmp.rotate(rotation)
            bmp.recycle()
            return rotated
        } else {
            bmp
        }
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

}
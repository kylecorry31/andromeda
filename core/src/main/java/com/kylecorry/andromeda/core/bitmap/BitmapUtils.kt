package com.kylecorry.andromeda.core.bitmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat.YUV_420_888
import android.graphics.Matrix
import android.media.Image
import com.google.android.renderscript.Toolkit
import com.google.android.renderscript.YuvFormat
import com.kylecorry.andromeda.core.units.PixelCoordinate
import com.kylecorry.sol.math.statistics.GLCM
import kotlin.math.max
import kotlin.math.roundToInt

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
        val bmp = if (format == YUV_420_888) {
            val yuvBuffer = YuvByteBuffer(this, null)
            val bytes = ByteArray(yuvBuffer.buffer.capacity())
            yuvBuffer.buffer.get(bytes)
            Toolkit.yuvToRgbBitmap(bytes, width, height, YuvFormat.NV21)
        } else {
            // From https://stackoverflow.com/questions/69151779/how-to-create-bitmap-from-android-mediaimage-in-output-image-format-rgba-8888-fo
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * width
            val bitmap = Bitmap.createBitmap(
                width + rowPadding / pixelStride,
                height, Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)
            bitmap
        }

        return if (rotation != 0f) {
            val rotated = bmp.rotate(rotation)
            bmp.recycle()
            return rotated
        } else {
            bmp
        }
    }

    fun Bitmap.convolve(kernel: FloatArray): Bitmap {
        return Toolkit.convolve(this, kernel)
    }

    fun Bitmap.gray(): Bitmap {
        return Toolkit.colorMatrix(this, Toolkit.greyScaleColorMatrix)
    }

    fun Bitmap.histogram(): IntArray {
        return Toolkit.histogram(this)
    }

    fun Bitmap.blur(radius: Int): Bitmap {
        return Toolkit.blur(this, radius)
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    fun Bitmap.resize(maxWidth: Int, maxHeight: Int): Bitmap {
        return if (maxHeight > 0 && maxWidth > 0) {
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            Bitmap.createScaledBitmap(this, finalWidth, finalHeight, true)
        } else {
            this
        }
    }

    // https://stackoverflow.com/questions/13161628/cropping-a-perspective-transformation-of-image-on-android
    fun Bitmap.fixPerspective(
        topLeft: PixelCoordinate,
        topRight: PixelCoordinate,
        bottomLeft: PixelCoordinate,
        bottomRight: PixelCoordinate
    ): Bitmap {
        // TODO: Do this in a way which doesn't reduce the size of the source bitmap - maybe just save the transformations and apply them later (this would allow for recalibration)
        val top = topLeft.distanceTo(topRight)
        val bottom = bottomLeft.distanceTo(bottomRight)
        val newWidth = (top + bottom) / 2f

        val left = topLeft.distanceTo(bottomLeft)
        val right = topRight.distanceTo(bottomRight)
        val newHeight = (left + right) / 2f

        val matrix = Matrix()
        matrix.setPolyToPoly(
            floatArrayOf(
                topLeft.x, topLeft.y,
                topRight.x, topRight.y,
                bottomRight.x, bottomRight.y,
                bottomLeft.x, bottomLeft.y,
            ),
            0,
            floatArrayOf(
                0f, 0f,
                newWidth, 0f,
                newWidth, newHeight,
                0f, newHeight
            ),
            0,
            4
        )

        val mappedTL = floatArrayOf(0f, 0f)
        matrix.mapPoints(mappedTL)
        val maptlx = mappedTL[0].roundToInt()
        val maptly = mappedTL[1].roundToInt()

        val mappedTR = floatArrayOf(width.toFloat(), 0f)
        matrix.mapPoints(mappedTR)
        val maptry = mappedTR[1].roundToInt()

        val mappedLL = floatArrayOf(0f, height.toFloat())
        matrix.mapPoints(mappedLL)
        val mapllx = mappedLL[0].roundToInt()

        val shiftX = max(-maptlx, -mapllx)
        val shiftY = max(-maptry, -maptly)

        val resultBitmap: Bitmap =
            Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        val newBitmap = Bitmap.createBitmap(
            resultBitmap,
            shiftX,
            shiftY,
            newWidth.toInt(),
            newHeight.toInt(),
            null,
            true
        )
        if (resultBitmap != newBitmap) {
            resultBitmap.recycle()
        }
        return newBitmap
    }

    fun Bitmap.glcm(
        step: Pair<Int, Int>,
        channel: ColorChannel,
        excludeTransparent: Boolean = false
    ): GLCM {
        // TODO: Make this faster with RenderScript
        val glcm = Array(256) { FloatArray(256) }

        var total = 0

        for (x in 0 until width) {
            for (y in 0 until height) {
                val neighborX = x + step.first
                val neighborY = y + step.second

                if (neighborX >= width || neighborX < 0) {
                    continue
                }

                if (neighborY >= height || neighborY < 0) {
                    continue
                }

                val currentPx = getPixel(x, y)
                val neighborPx = getPixel(neighborX, neighborY)

                if (excludeTransparent && currentPx.getChannel(ColorChannel.Alpha) != 255) {
                    continue
                }

                if (excludeTransparent && neighborPx.getChannel(ColorChannel.Alpha) != 255) {
                    continue
                }

                val current = currentPx.getChannel(channel)
                val neighbor = neighborPx.getChannel(channel)

                glcm[current][neighbor]++
                total++
            }
        }

        if (total > 0) {
            for (row in glcm.indices) {
                for (col in glcm[0].indices) {
                    glcm[row][col] /= total.toFloat()
                }
            }
        }


        return glcm
    }

    private fun Int.getChannel(channel: ColorChannel): Int {
        return when (channel) {
            ColorChannel.Red -> Color.red(this)
            ColorChannel.Green -> Color.green(this)
            ColorChannel.Blue -> Color.blue(this)
            ColorChannel.Alpha -> Color.alpha(this)
        }
    }

}
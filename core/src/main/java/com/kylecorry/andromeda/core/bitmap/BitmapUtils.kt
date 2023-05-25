package com.kylecorry.andromeda.core.bitmap

import android.graphics.*
import android.graphics.BitmapFactory.Options
import android.graphics.ImageFormat.YUV_420_888
import android.media.Image
import android.util.Size
import androidx.annotation.ColorInt
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.get
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.google.android.renderscript.LookupTable
import com.google.android.renderscript.Toolkit
import com.google.android.renderscript.YuvFormat
import com.kylecorry.andromeda.core.units.PixelCoordinate
import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.algebra.createMatrix
import java.io.InputStream
import kotlin.math.roundToInt

object BitmapUtils {

    fun decodeBitmapScaled(
        path: String,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap? {
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, this)
            inSampleSize = calculateInSampleSize(this, maxWidth, maxHeight)
            inJustDecodeBounds = false
            BitmapFactory.decodeFile(path, this)
        }
    }

    /**
     * Decodes a region of a bitmap. If the region is odd, Android may not respect the size.
     * @param stream The stream to decode
     * @param region The region to decode
     * @param options The options to use when decoding
     * @param autoClose Whether to close the stream after decoding
     */
    fun decodeRegion(
        stream: InputStream,
        region: Rect,
        options: Options? = null,
        autoClose: Boolean = false
    ): Bitmap? {
        try {
            val decoder = if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.R) {
                BitmapRegionDecoder.newInstance(stream)
            } else {
                @Suppress("DEPRECATION")
                BitmapRegionDecoder.newInstance(stream, false)
            } ?: return null
            return decoder.decodeRegion(region, options)
        } finally {
            if (autoClose) {
                stream.close()
            }
        }
    }

    fun getBitmapSize(path: String): Size? {
        val opts = BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, this)
            this
        }
        val width = opts.outWidth
        val height = opts.outHeight

        if (width == -1 || height == -1) return null

        return Size(width, height)
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

    fun Bitmap.resizeExact(width: Int, height: Int): Bitmap {
        return Toolkit.resize(this, width, height)
    }

    fun Bitmap.resizeToFit(maxWidth: Int, maxHeight: Int): Bitmap {
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

    fun Bitmap.fixPerspective(
        topLeft: PixelCoordinate,
        topRight: PixelCoordinate,
        bottomLeft: PixelCoordinate,
        bottomRight: PixelCoordinate,
        shouldRecycleOriginal: Boolean = false,
        @ColorInt backgroundColor: Int? = null
    ): Bitmap {
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

        // Create an empty mutable bitmap
        val blank = Bitmap.createBitmap(newWidth.toInt(), newHeight.toInt(), config)
        // Create a canvas to draw on
        val canvas = Canvas(blank)

        if (backgroundColor != null) {
            canvas.drawColor(backgroundColor)
        }

        // Apply matrix to canvas
        canvas.concat(matrix)

        canvas.drawBitmap(this, 0f, 0f, null)

        if (shouldRecycleOriginal) {
            this.recycle()
        }

        return blank
    }

    fun Bitmap.crop(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        @ColorInt backgroundColor: Int? = null
    ): Bitmap {
        // Create an empty mutable bitmap
        val blank = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
        // Create a canvas to draw on
        val canvas = Canvas(blank)

        if (backgroundColor != null) {
            canvas.drawColor(backgroundColor)
        }

        // Draw the source bitmap onto the canvas
        canvas.drawBitmap(this, -x, -y, null)

        return blank
    }

    fun Bitmap.quantize(bins: Int): Bitmap {
        return quantize(bins, bins, bins, bins)
    }

    fun Bitmap.quantize(redBins: Int, blueBins: Int, greenBins: Int, alphaBins: Int): Bitmap {
        val table = LookupTable()
        quantize(table.red, redBins)
        quantize(table.green, greenBins)
        quantize(table.blue, blueBins)
        quantize(table.alpha, alphaBins)
        return Toolkit.lut(this, table)
    }

    private fun quantize(arr: ByteArray, bins: Int) {
        if (bins >= 256 || bins <= 0) {
            return
        }
        for (i in arr.indices) {
            arr[i] = quantize(arr[i], bins)
        }
    }

    private fun quantize(value: Byte, bins: Int): Byte {
        if (bins == 256) {
            return value
        }

        if (bins <= 0) {
            return 0
        }
        return (SolMath.map(value.toFloat(), 0f, 255f, 0f, bins - 1f)).roundToInt().toByte()
    }

    private fun quantize(value: Int, bins: Int): Int {
        if (bins == 256) {
            return value
        }

        if (bins <= 0) {
            return 0
        }
        return (SolMath.map(value.toFloat(), 0f, 255f, 0f, bins - 1f)).roundToInt()
    }

    /**
     * Calculate the Gray-Level Co-Occurrence Matrix (GLCM) of a bitmap. For best results, convert the image to grayscale.
     * @param steps the step size and direction (X, Y pixels) to calculate the GLCM for
     * @param channel the color channel to calculate the GLCM for
     * @param excludeTransparent if true, transparent pixels will be excluded from the GLCM
     * @param symmetric if true, when (i, j) is found, (j, i) will also be incremented
     * @param normed if true, the matrix will sum up to 1
     * @param levels the levels of gray for each pixel, defaults to 256
     */
    fun Bitmap.glcm(
        steps: List<Pair<Int, Int>>,
        channel: ColorChannel,
        excludeTransparent: Boolean = false,
        symmetric: Boolean = false,
        normed: Boolean = true,
        levels: Int = 256,
        region: Rect? = null
    ): com.kylecorry.sol.math.algebra.Matrix {
        // TODO: Make this faster with RenderScript
        val glcm = createMatrix(levels, levels, 0f)

        var total = 0

        val startX = (region?.left ?: 0).coerceIn(0, width)
        val endX = (region?.right ?: width).coerceIn(0, width)

        val startY = (region?.top ?: 0).coerceIn(0, height)
        val endY = (region?.bottom ?: height).coerceIn(0, height)

        for (x in startX until endX) {
            for (y in startY until endY) {
                for (step in steps) {
                    val neighborX = x + step.first
                    val neighborY = y + step.second

                    if (neighborX >= endX || neighborX < startX) {
                        continue
                    }

                    if (neighborY >= endY || neighborY < startY) {
                        continue
                    }

                    val currentPx = getPixel(x, y)

                    if (excludeTransparent && currentPx.getChannel(ColorChannel.Alpha) != 255) {
                        continue
                    }

                    val neighborPx = getPixel(neighborX, neighborY)

                    if (excludeTransparent && neighborPx.getChannel(ColorChannel.Alpha) != 255) {
                        continue
                    }

                    val current = quantize(currentPx.getChannel(channel), levels)
                    val neighbor = quantize(neighborPx.getChannel(channel), levels)

                    glcm[current][neighbor]++
                    total++
                    if (symmetric) {
                        glcm[neighbor][current]++
                        total++
                    }
                }
            }
        }

        if (normed && total > 0) {
            for (row in glcm.indices) {
                for (col in glcm[0].indices) {
                    glcm[row][col] /= total.toFloat()
                }
            }
        }


        return glcm
    }

    fun Int.getChannel(channel: ColorChannel): Int {
        return when (channel) {
            ColorChannel.Red -> Color.red(this)
            ColorChannel.Green -> Color.green(this)
            ColorChannel.Blue -> Color.blue(this)
            ColorChannel.Alpha -> Color.alpha(this)
        }
    }

    @ColorInt
    fun Bitmap.nearestPixel(x: Float, y: Float): Int? {
        return nearestPixel(x.roundToInt(), y.roundToInt())
    }

    @ColorInt
    fun Bitmap.nearestPixel(x: Int, y: Int): Int? {
        val x1 = x.coerceIn(0, width - 1)
        val y1 = y.coerceIn(0, height - 1)

        if (!isInBounds(x1, y1)) {
            return null
        }

        return this[x1, y1]
    }

    @ColorInt
    fun Bitmap.interpolateBilinear(x: Float, y: Float): Int? {
        val x1 = x.toInt()
        val x2 = x1 + 1
        val y1 = y.toInt()
        val y2 = y1 + 1

        if (!isInBounds(x1, y1) || !isInBounds(x2, y2)) {
            return null
        }

        val x1y1 = this[x1, y1]
        val x1y2 = this[x1, y2]
        val x2y1 = this[x2, y1]
        val x2y2 = this[x2, y2]

        val x1y1Weight = (x2 - x) * (y2 - y)
        val x1y2Weight = (x2 - x) * (y - y1)
        val x2y1Weight = (x - x1) * (y2 - y)
        val x2y2Weight = (x - x1) * (y - y1)

        val red =
            x1y1.red * x1y1Weight + x1y2.red * x1y2Weight + x2y1.red * x2y1Weight + x2y2.red * x2y2Weight
        val green =
            x1y1.green * x1y1Weight + x1y2.green * x1y2Weight + x2y1.green * x2y1Weight + x2y2.green * x2y2Weight
        val blue =
            x1y1.blue * x1y1Weight + x1y2.blue * x1y2Weight + x2y1.blue * x2y1Weight + x2y2.blue * x2y2Weight
        val alpha =
            x1y1.alpha * x1y1Weight + x1y2.alpha * x1y2Weight + x2y1.alpha * x2y1Weight + x2y2.alpha * x2y2Weight

        return Color.argb(alpha.toInt(), red.toInt(), green.toInt(), blue.toInt())
    }

    fun Bitmap.isInBounds(x: Int, y: Int): Boolean {
        return x in 0 until width && y in 0 until height
    }

}
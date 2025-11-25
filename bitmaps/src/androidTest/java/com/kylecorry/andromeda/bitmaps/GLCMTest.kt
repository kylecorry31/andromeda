package com.kylecorry.andromeda.bitmaps

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.roundToInt
import kotlin.random.Random

class GLCMTest {


    @Test
    fun glcm() {
        val zeros = createBitmap()
        val random = createBitmap()

        val r = Random(1)
        for (x in 0 until random.width) {
            for (y in 0 until random.height) {
                random.setPixel(x, y, Color.rgb(r.nextInt(), r.nextInt(), r.nextInt()))
            }
        }

        arraysEqual(
            zeros.glcm(
                listOf(0 to 1, 1 to 0, -1 to 0, 0 to -1),
                ColorChannel.Red,
                symmetric = true,
                normed = true,
                levels = 100
            ),
            Toolkit.glcm(
                zeros, 100, 0,
                symmetric = true,
                normalize = true,
                excludeTransparent = false,
                steps = intArrayOf(0, 1, 1, 0, -1, 0, 0, -1)
            )
        )

        arraysEqual(
            random.glcm(
                listOf(0 to 1, 1 to 0, -1 to 0, 0 to -1),
                ColorChannel.Red,
                symmetric = true,
                normed = true,
                levels = 100
            ),
            Toolkit.glcm(
                random, 100, 0,
                symmetric = true,
                normalize = true,
                excludeTransparent = false,
                steps = intArrayOf(0, 1, 1, 0, -1, 0, 0, -1)
            )
        )

        arraysEqual(
            random.glcm(
                listOf(0 to 1),
                ColorChannel.Green,
                symmetric = true,
                normed = true,
                levels = 256,
                region = Rect(0, 0, 50, 50)
            ),
            Toolkit.glcm(
                random, 256, 1,
                symmetric = true,
                normalize = true,
                excludeTransparent = false,
                steps = intArrayOf(0, 1),
                restriction = Range2d(0, 51, 0, 51)
            )
        )


    }

    private fun arraysEqual(a: FloatArray, b: FloatArray, epsilon: Float = 0.001f) {
        assertEquals(a.size, b.size)
        for (i in a.indices) {
            assertEquals(a[i], b[i], epsilon)
        }
    }

    private fun Bitmap.glcm(
        steps: List<Pair<Int, Int>>,
        channel: ColorChannel,
        excludeTransparent: Boolean = false,
        symmetric: Boolean = false,
        normed: Boolean = true,
        levels: Int = 256,
        region: Rect? = null
    ): FloatArray {
        val glcm = Array(levels) { Array(levels) { 0f } }

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

                    if (neighborX !in startX..<endX) {
                        continue
                    }

                    if (neighborY !in startY..<endY) {
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
                    glcm[row][col] = (glcm[row][col] / total.toDouble()).toFloat()
                }
            }
        }


        return glcm.flatten().toFloatArray()
    }

    private fun Int.getChannel(channel: ColorChannel): Int {
        return when (channel) {
            ColorChannel.Red -> Color.red(this)
            ColorChannel.Green -> Color.green(this)
            ColorChannel.Blue -> Color.blue(this)
            ColorChannel.Alpha -> Color.alpha(this)
        }
    }

    private enum class ColorChannel {
        Red,
        Green,
        Blue,
        Alpha
    }

    private fun quantize(value: Int, bins: Int): Int {
        if (bins == 256) {
            return value
        }

        if (bins <= 0) {
            return 0
        }
        return ((value / 255f) * (bins - 1)).roundToInt()
    }

    private fun createBitmap(width: Int = 100, height: Int = 100): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }
}
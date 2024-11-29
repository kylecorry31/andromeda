package com.kylecorry.andromeda.core.system

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.kylecorry.andromeda.core.bitmap.BitmapUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.min

class BitmapUtilsTest {

    @Test
    fun decodeRegionEnforceBounds() {
        // Create a 24x24
        val bitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888)
        for (x in 0 until 24) {
            for (y in 0 until 24) {
                val color = if (x % 2 == 0) 0 else Color.rgb(x * 100, y * 100, 0)
                bitmap.setPixel(x, y, color)
            }
        }

        // Save it to a file
        val pngOut = ByteArrayOutputStream()
        val jpgOut = ByteArrayOutputStream()
        val webpLosslessOut = ByteArrayOutputStream()
        val webpLossyOut = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, pngOut)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, jpgOut)
        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, webpLosslessOut)
        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 100, webpLossyOut)

        // Attempt to load the regions
        val pngIn = ByteArrayInputStream(pngOut.toByteArray())
        val jpgIn = ByteArrayInputStream(jpgOut.toByteArray())
        val webpLosslessIn = ByteArrayInputStream(webpLosslessOut.toByteArray())
        val webpLossyIn = ByteArrayInputStream(webpLossyOut.toByteArray())

        for (l in 0..<bitmap.width) {
            for (t in 0..<bitmap.height) {
                pngIn.reset()
                jpgIn.reset()
                webpLosslessIn.reset()
                webpLossyIn.reset()
                val region = Rect(l, t, min(l + 5, bitmap.width), min(t + 3, bitmap.height))
                val enforceBounds = true

                val pngRegion = BitmapUtils.decodeRegion(
                    pngIn,
                    region,
                    enforceBounds = enforceBounds
                )!!
                val jpgRegion = BitmapUtils.decodeRegion(
                    jpgIn,
                    region,
                    enforceBounds = enforceBounds
                )!!
                val webpLosslessRegion = BitmapUtils.decodeRegion(
                    webpLosslessIn,
                    region,
                    enforceBounds = enforceBounds
                )!!
                val webpLossyRegion = BitmapUtils.decodeRegion(
                    webpLossyIn,
                    region,
                    enforceBounds = enforceBounds
                )!!

                // Verify the regions are correct
                assertEquals(region.width(), pngRegion.width)
                assertEquals(region.height(), pngRegion.height)
                assertEquals(region.width(), jpgRegion.width)
                assertEquals(region.height(), jpgRegion.height)
                assertEquals(region.width(), webpLosslessRegion.width)
                assertEquals(region.height(), webpLosslessRegion.height)
                assertEquals(region.width(), webpLossyRegion.width)
                assertEquals(region.height(), webpLossyRegion.height)

                for (x in 0 until region.width()) {
                    for (y in 0 until region.height()) {
                        val source = bitmap.getPixel(x + region.left, y + region.top)
                        val red = source.red
                        val green = source.green
                        val lossLessDelta = 0f
                        val lossyDelta = 120f
                        assertEquals(
                            red.toFloat(),
                            pngRegion.getPixel(x, y).red.toFloat(),
                            lossLessDelta
                        )
                        assertEquals(
                            green.toFloat(),
                            pngRegion.getPixel(x, y).green.toFloat(),
                            lossLessDelta
                        )
                        assertEquals(
                            red.toFloat(),
                            jpgRegion.getPixel(x, y).red.toFloat(),
                            lossyDelta
                        )
                        assertEquals(
                            green.toFloat(),
                            jpgRegion.getPixel(x, y).green.toFloat(),
                            lossyDelta
                        )
                        assertEquals(
                            red.toFloat(),
                            webpLosslessRegion.getPixel(x, y).red.toFloat(),
                            lossLessDelta
                        )
                        assertEquals(
                            green.toFloat(),
                            webpLosslessRegion.getPixel(x, y).green.toFloat(),
                            lossLessDelta
                        )
                        assertEquals(
                            red.toFloat(),
                            webpLossyRegion.getPixel(x, y).red.toFloat(),
                            lossyDelta
                        )
                        assertEquals(
                            green.toFloat(),
                            webpLossyRegion.getPixel(x, y).green.toFloat(),
                            lossyDelta
                        )
                    }
                }
            }
        }
    }

}
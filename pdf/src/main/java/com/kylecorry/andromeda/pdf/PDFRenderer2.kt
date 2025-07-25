package com.kylecorry.andromeda.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Size
import androidx.annotation.ColorInt
import com.kylecorry.andromeda.core.system.Screen
import androidx.core.graphics.createBitmap

class PDFRenderer2(
    private val context: Context,
    private val uri: Uri,
    private val inchesToPixels: Float? = null,
    private val config: Bitmap.Config = Bitmap.Config.RGB_565,
) {

    private val dpi = Screen.dpi(context)
    private val lock = Any()

    fun getSize(): Size {
        return context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
            PdfRenderer(fd).use { renderer ->
                val page = renderer.openPage(0)
                val size = Size(page.width, page.height)
                page.close()
                size
            }
        } ?: Size(0, 0)
    }

    fun toBitmap(
        outputSize: Size,
        page: Int = 0,
        @ColorInt backgroundColor: Int = Color.WHITE,
        srcRegion: RectF? = null
    ): Bitmap? {
        synchronized(lock) {
            return context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                PdfRenderer(fd).use { renderer ->

                    val pageCount = renderer.pageCount
                    if (page >= pageCount) {
                        return null
                    }

                    val pdfPage = renderer.openPage(page)

                    val bitmap =
                        createBitmap(outputSize.width, outputSize.height)
                    if (backgroundColor != Color.TRANSPARENT) {
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(backgroundColor)
                    }

                    val transform = if (srcRegion != null) {
                        val matrix = Matrix()

                        val dpiScale = inchesToPixels ?: (dpi / 72f)

                        // Scale the PDF to the screen DPI
                        matrix.postScale(dpiScale, dpiScale)

                        // Translate the PDF to the top left corner
                        matrix.postTranslate(-srcRegion.left, -srcRegion.top)

                        // Scale the PDF to the output size
                        matrix.postScale(
                            outputSize.width.toFloat() / srcRegion.width(),
                            outputSize.height.toFloat() / srcRegion.height()
                        )

                        matrix
                    } else {
                        null
                    }

                    pdfPage.render(
                        bitmap,
                        null,
                        transform,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )

                    pdfPage.close()

                    // Convert the bitmap to rgb 565
                    if (config == bitmap.config) {
                        return bitmap
                    }

                    val reconfigured = bitmap.copy(config, false)
                    bitmap.recycle()
                    reconfigured
                }
            }
        }
    }
}
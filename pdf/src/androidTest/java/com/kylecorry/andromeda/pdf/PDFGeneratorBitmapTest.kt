package com.kylecorry.andromeda.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import java.io.File


internal class PDFGeneratorBitmapTest {

    @Test
    fun generate() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawColor(Color.BLUE)

        val temp = File.createTempFile("test", ".jpg")
        temp.deleteOnExit()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, temp.outputStream())

        val objects = listOf(
            catalog(1, 2),
            pages(2, listOf(3)),
            page(3, 2, 200, 200, listOf(4)),
            temp.inputStream().use { jpg(4, it, 200, 200, 0, 0) }
        )

        val output = File(context.filesDir, "test.pdf")
        val generator = PDFGenerator()
        generator.toPDF(objects, output.outputStream())
    }
}
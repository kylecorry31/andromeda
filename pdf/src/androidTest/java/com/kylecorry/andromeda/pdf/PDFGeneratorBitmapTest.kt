package com.kylecorry.andromeda.pdf

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

        val objects = listOf(
            catalog("1 0", "2 0"),
            pages("2 0", listOf("3 0")),
            page("3 0", "2 0", 200, 200, listOf("4 0")),
            image("4 0", bitmap, 0, 0, 200, 200)
        )

        val output = File(context.filesDir, "test.pdf")
        val generator = PDFGenerator()
        generator.toPDF(objects, output.outputStream())
    }
}
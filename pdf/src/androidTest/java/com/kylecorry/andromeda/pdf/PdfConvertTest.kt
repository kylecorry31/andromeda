package com.kylecorry.andromeda.pdf

import android.text.Html
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File

class PdfConvertTest {

    @Test
    fun toPDF() {
        val text = Html.fromHtml(
            "<h1>Hello, World!</h1><p>This is a test</p>".repeat(1000),
            Html.FROM_HTML_MODE_COMPACT
        )
        val outputStream = ByteArrayOutputStream()
        val context = InstrumentationRegistry.getInstrumentation().context
        PdfConvert.toPdf(text, outputStream)

        val pdfStringUtf8 = outputStream.toString("UTF-8")
        println(pdfStringUtf8)

        // Write to file
        val file = File(context.filesDir, "test.pdf")
        file.writeBytes(outputStream.toByteArray())
    }

}
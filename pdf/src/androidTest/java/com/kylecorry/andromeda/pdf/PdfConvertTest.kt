package com.kylecorry.andromeda.pdf

import android.text.Html
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File

class PdfConvertTest {

    @Test
    fun toPDF() {
        val text = Html.fromHtml((0..1000).joinToString("\n") { "<h1>Test sejpfoisje piofjspeoifhj soeihfjspi ejfpsuehijf psoiejf ;slejfposiejf posiejfop;s</h1><p>$it</p>" })
        val outputStream = ByteArrayOutputStream()
        val context = InstrumentationRegistry.getInstrumentation().context
        PdfConvert.toPdf(context, text, outputStream)

        // Write to file
        val file = File(context.filesDir, "test.pdf")
        file.writeBytes(outputStream.toByteArray())
    }

}
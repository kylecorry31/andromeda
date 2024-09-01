package com.kylecorry.andromeda.pdf

import android.content.Context
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.io.InputStream
import java.io.OutputStream


object PdfConvert {

    private val generator = PDFGenerator()
    private val parser = PDFParser()

    fun toPDF(objects: List<PDFObject>): String {
        return generator.toPDF(objects)
    }

    fun toPDF(objects: List<PDFObject>, out: OutputStream) {
        generator.toPDF(objects, out)
    }

    fun fromPDF(input: InputStream, ignoreStreams: Boolean = false): List<PDFObject> {
        return parser.parse(input, ignoreStreams)
    }

    fun toPdf(
        context: Context,
        text: CharSequence,
        out: OutputStream,
        textSize: Float = 6f,
        margins: Int = 50
    ) {
        val textView = TextView(context)
        textView.text = text
        textView.setTextColor(Color.BLACK)
        textView.textSize = textSize
        toPdf(textView, out, margins)
    }

    fun toPdf(textView: TextView, out: OutputStream, margins: Int = 50) {
        val document = PdfDocument()
        val pageInfo = PageInfo.Builder(595, 842, 1).create()
        val text = textView.text
        textView.layoutParams = ViewGroup.LayoutParams(
            pageInfo.pageWidth - margins * 2,
            pageInfo.pageHeight - margins * 2
        )

        val widthSpec =
            View.MeasureSpec.makeMeasureSpec(
                pageInfo.pageWidth - margins * 2,
                View.MeasureSpec.EXACTLY
            )
        val heightSpec =
            View.MeasureSpec.makeMeasureSpec(
                pageInfo.pageHeight - margins * 2,
                View.MeasureSpec.EXACTLY
            )
        textView.measure(widthSpec, heightSpec)

        textView.layout(0, 0, textView.measuredWidth, textView.measuredHeight)

        var startPos = 0
        var endPos = text.length

        do {
            endPos = getLastVisibleCharPos(textView, pageInfo, text, startPos, endPos, margins)
            textView.text = text.subSequence(startPos, endPos)
            textView.onPreDraw()

            // Step 3: Draw the text to the page
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            canvas.translate(margins.toFloat(), margins.toFloat())
            textView.draw(canvas)
            document.finishPage(page)

            // Step 4: Update the start and end positions
            startPos = endPos
            endPos = text.length
        } while (startPos < text.length)

        document.writeTo(out)
    }


    private fun getLastVisibleCharPos(
        textView: TextView,
        pageInfo: PageInfo,
        text: CharSequence,
        startPos: Int,
        endPos: Int,
        margins: Int
    ): Int {
        textView.text = text.subSequence(startPos, endPos)
        textView.onPreDraw()
        var line = textView.layout.getLineForVertical(pageInfo.pageHeight - margins * 2)

        while (line > 0 && textView.layout.getLineBottom(line) > pageInfo.pageHeight - margins * 2) {
            line--
        }

        return startPos + textView.layout.getLineEnd(line)
    }

}
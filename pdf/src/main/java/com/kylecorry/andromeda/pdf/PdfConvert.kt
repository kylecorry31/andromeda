package com.kylecorry.andromeda.pdf

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.print.pdf.PrintedPdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.text.toSpanned
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

    @RequiresApi(Build.VERSION_CODES.M)
    fun toPdf(text: CharSequence, out: OutputStream) {

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = document.startPage(pageInfo)

        page = printLongText(document, pageInfo, text, page, 1, 50)

        document.finishPage(page)

        document.writeTo(out)
    }

    // https://stackoverflow.com/questions/78221287/writing-long-text-to-pdf-using-the-pdfdocument-class
    // TODO: Not filling the vertical space of the page
    @RequiresApi(Build.VERSION_CODES.M)
    private fun printLongText(
        mDocument: PdfDocument,
        pageInfo: PdfDocument.PageInfo,
        spanText: CharSequence,
        page: PdfDocument.Page,
        pageNr: Int,
        yPos: Int
    ): PdfDocument.Page {
        var lPage: PdfDocument.Page = page
        var lLineCount = 34
        val textPaint = TextPaint()

        val mPageWidth = pageInfo.pageWidth
        val mPageHeight = pageInfo.pageHeight
        val cTopBottomMargin = 50
        val cLeftRightMargin = 50

        if (pageNr > 1) {
            // close previous and start new page
            mDocument.finishPage(lPage)            // close this page
            lPage = mDocument.startPage(pageInfo) // start new page
            lLineCount = 38                       // correct line count (no header so more space)
        }
        val canvas = lPage.canvas                 // init canvas

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 12f
        textPaint.color = Color.BLACK
        textPaint.isUnderlineText = false

        // first build a static layout of the complete available text to determine which part fits the page
        var staticLayout = StaticLayout.Builder
            .obtain(spanText, 0, spanText.length, textPaint, mPageWidth - (2 * cLeftRightMargin))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.0f)
            .setIncludePad(false)
            .build()

        // create a Rect for the available space
        val r = Rect(
            cLeftRightMargin,
            yPos,
            (mPageWidth - (2 * cLeftRightMargin)),
            mPageHeight - cTopBottomMargin - yPos
        )

        var lLastPos = try {
            staticLayout.getLineBounds(lLineCount, r)
            // we really do not care about the result of the above getLineBounds
            // we only care if it does or doesn't raise an exception
            // in case of an exception all the text is on this page and we are done.
            // in case of no exception there is more text after the lineCount
            staticLayout.getLineEnd(lLineCount)
        } catch (e: Exception) {
            spanText.length
        }

        // sometimes GetLineEnd returns 0. if so all the remaining text fits
        if (lLastPos == 0) lLastPos = spanText.length

        // re-build the layout to contain only the part we want to print on this page
        staticLayout = StaticLayout.Builder
            .obtain(spanText, 0, lLastPos, textPaint, mPageWidth - (2 * cLeftRightMargin))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.0f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(cLeftRightMargin.toFloat(), yPos.toFloat())
        staticLayout.draw(canvas)
        canvas.restore()

        // check if we are done and if not do a recursive call to handle the remainder of the text
        if (lLastPos < spanText.length) {
            lPage = printLongText(
                mDocument,
                pageInfo,
                spanText.subSequence(lLastPos, spanText.length),
                lPage,
                pageNr + 1,
                cTopBottomMargin
            )
        }

        return lPage
    }
}
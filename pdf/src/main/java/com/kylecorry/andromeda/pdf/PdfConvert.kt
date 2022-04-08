package com.kylecorry.andromeda.pdf

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

}
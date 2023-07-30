package com.kylecorry.andromeda.pdf

import com.kylecorry.andromeda.core.io.ISerializer
import java.io.InputStream
import java.io.OutputStream

class PDFSerializer(private val ignoreStreams: Boolean = false) : ISerializer<List<PDFObject>> {
    override fun serialize(obj: List<PDFObject>, stream: OutputStream) {
        PdfConvert.toPDF(obj, stream)
    }

    override fun deserialize(stream: InputStream): List<PDFObject> {
        return PdfConvert.fromPDF(stream, ignoreStreams)
    }
}
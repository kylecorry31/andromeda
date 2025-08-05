package com.kylecorry.andromeda.pdf

import com.kylecorry.andromeda.core.io.ISerializer
import com.kylecorry.andromeda.core.io.SerializationException
import java.io.InputStream
import java.io.OutputStream

class PDFSerializer(private val ignoreStreams: Boolean = false) : ISerializer<List<PDFValue.PDFObject>> {
    override fun serialize(obj: List<PDFValue.PDFObject>, stream: OutputStream) {
        try {
            PdfConvert.toPDF(obj, stream)
        } catch (e: Exception) {
            throw SerializationException(e.message ?: "Unknown error", e)
        }
    }

    override fun deserialize(stream: InputStream): List<PDFValue.PDFObject> {
        return try {
            PdfConvert.fromPDF(stream, ignoreStreams)
        } catch (e: Exception) {
            throw SerializationException(e.message ?: "Unknown error", e)
        }
    }
}
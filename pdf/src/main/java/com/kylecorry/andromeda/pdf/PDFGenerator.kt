package com.kylecorry.andromeda.pdf

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.Writer

internal class PDFGenerator {

    fun toPDF(objects: List<PDFValue.PDFObject>, out: OutputStream) {
        val sortedObjects = objects.sortedBy { it.id }
        val writer = out.bufferedWriter()
        val xref = mutableListOf<Long>()
        val root = sortedObjects.getByProperty("/Type", name("/Catalog")).first().reference
        var size = 0L
        size += append(writer, "%PDF-1.3\n")
        writer.flush()

        for (obj in sortedObjects) {
            xref.add(size)
            size += obj.write(out)
        }

        val startXref = size
        // The xref table and /Size include the free entry for object 0
        val entryCount = xref.size + 1
        // Each xref entry must be exactly 20 bytes, including the trailing space and newline
        append(writer, "xref\n0 $entryCount\n0000000000 65535 f \n")
        append(writer, xref.joinToString("") { "${it.toString().padStart(10, '0')} 00000 n \n" })
        append(
            writer,
            "trailer\n<<\n/Size $entryCount\n/Root $root\n>>\nstartxref\n$startXref\n%%EOF"
        )
        writer.flush()
    }

    private fun append(out: Writer, text: String): Int {
        out.write(text)
        return text.length
    }

    fun toPDF(objects: List<PDFValue.PDFObject>): String {
        return ByteArrayOutputStream().use {
            toPDF(objects, it)
            it.toString()
        }
    }

}
package com.kylecorry.andromeda.pdf

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.Writer

internal class PDFGenerator {

    fun toPDF(objects: List<PDFValue.PDFObject>, out: OutputStream) {
        val sortedObjects = objects.sortedBy { it.id }
        val writer = out.bufferedWriter()
        val xref = mutableListOf<Int>()
        val root = sortedObjects.getByProperty("/Type", name("/Catalog")).first().reference
        var size = 0
        size += append(writer, "%PDF-1.3\n")
        writer.flush()

        for (obj in sortedObjects) {
            xref.add(size)
            size += appendBytes(out, obj.toByteArray())
            size += appendBytes(out, "\n".toByteArray())
        }

        val startXref = size
        append(writer, "xref\n0 ${xref.size}\n0000000000 65535 f\n")
        append(writer, xref.joinToString("\n") { "${it.toString().padStart(10, '0')} 00000 n" })
        append(
            writer,
            "\ntrailer\n<<\n/Size ${xref.size}\n/Root $root\n>>\nstartxref\n$startXref\n%%EOF"
        )
        writer.flush()
    }

    private fun append(out: Writer, text: String): Int {
        out.write(text)
        return text.length
    }

    private fun appendBytes(out: OutputStream, arr: ByteArray): Int {
        out.write(arr)
        return arr.size
    }

    fun toPDF(objects: List<PDFValue.PDFObject>): String {
        return ByteArrayOutputStream().use {
            toPDF(objects, it)
            it.toString()
        }
    }

}
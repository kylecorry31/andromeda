package com.kylecorry.andromeda.pdf

import java.io.*

internal class PDFGenerator {

    fun toPDF(objects: List<PDFObject>, out: OutputStream) {
        val sortedObjects = objects.sortedBy { it.id }
        val writer = out.bufferedWriter()
        val xref = mutableListOf<Int>()
        val root = sortedObjects.getByProperty("/Type", "/Catalog").first().id
        var size = 0
        size += append(writer, "%PDF-1.3\n")

        for (obj in sortedObjects) {
            xref.add(size)
            size += append(writer, "${obj.id} obj\n")
            val properties = "<<\n" + obj.properties.joinToString("\n") + "\n>>\n"
            size += append(writer, properties)

            for (stream in obj.streams){
                size += append(writer, "stream\n")
                writer.flush()
                size += appendBytes(out, stream)
                size += append(writer, "\nendstream\n")
            }
            size += append(writer, "endobj\n")
        }

        val startXref = size
        append(writer, "xref\n0 ${xref.size}\n0000000000 65535 f\n")
        append(writer, xref.joinToString("\n") { "${it.toString().padStart(10, '0')} 00000 n" })
        append(
            writer,
            "\ntrailer\n<<\n/Size ${xref.size}\n/Root $root R\n>>\nstartxref\n$startXref\n%%EOF"
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

    fun toPDF(objects: List<PDFObject>): String {
        return ByteArrayOutputStream().use {
            toPDF(objects, it)
            it.toString()
        }
    }

}
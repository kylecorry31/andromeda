package com.kylecorry.andromeda.pdf

import java.io.*

internal class PDFGenerator {

    fun toPDF(objects: List<PDFObject>, out: OutputStream) {
        val writer = out.bufferedWriter()
        val xref = mutableListOf<Int>()
        val root = objects.getByProperty("/Type", "/Catalog").first().id
        var size = 0
        size += append(writer, "%PDF-1.3\n")

        for (obj in objects) {
            xref.add(size)
            size += append(writer, "${obj.id} obj\n")
            val properties = "<<\n" + obj.properties.joinToString("\n") + "\n>>\n"
            size += append(writer, properties)
            val streams = obj.streams.joinToString("\n") { "stream\n$it\nendstream\n" }
            size += append(writer, streams)
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

    fun toPDF(objects: List<PDFObject>): String {
        return ByteArrayOutputStream().use {
            toPDF(objects, it)
            it.toString()
        }
    }

}
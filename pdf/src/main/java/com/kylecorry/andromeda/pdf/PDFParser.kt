package com.kylecorry.andromeda.pdf

import com.kylecorry.andromeda.core.io.readLine
import com.kylecorry.andromeda.core.io.readUntil
import com.kylecorry.sol.math.RingBuffer
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.Buffer

internal class PDFParser {

    fun parse(pdf: InputStream, ignoreStreams: Boolean = false): List<PDFObject> {
        val reader = pdf.bufferedReader()
        val objects = mutableListOf<PDFObject>()
        val builder = StringBuilder()
        var b: Int
        while (reader.read().also { b = it } != -1) {
            builder.append(b.toChar())

            if (b.toChar() == '\n') {
                builder.clear()
            }

            if (builder.toString().endsWith("obj")) {
                objects.add(parseObject(builder.toString(), reader, !ignoreStreams))
                builder.clear()
            }
        }
        return objects
    }

    private fun parseObject(
        startToken: String,
        stream: BufferedReader,
        shouldParseStreams: Boolean
    ): PDFObject {
        val properties = mutableListOf<String>()
        val streams = mutableListOf<ByteArray>()
        val id = startToken.replace("obj", "").trim()
        val builder = StringBuilder()
        var b: Int
        while (stream.read().also { b = it } != -1) {
            builder.append(b.toChar())
            // If << is encountered, parse properties
            if (builder.toString().endsWith("<<")) {
                builder.clear()
                properties.addAll(parseProperties(stream))
            }
            // If stream is encountered, parse stream
            if (builder.toString().endsWith("stream")) {
                builder.clear()
                streams.add(parseStream(stream, shouldParseStreams).toByteArray())
            }
            // If endobj is encountered, return
            if (builder.toString().endsWith("endobj")) {
                return PDFObject(id, properties, if (shouldParseStreams) streams else emptyList())
            }
        }
        return PDFObject(id, properties, if (shouldParseStreams) streams else emptyList())
    }

    private fun parseStream(stream: BufferedReader, shouldParse: Boolean): String {
        return readUntil(stream, "endstream", shouldParse)
    }

    // TODO: Extract this
    private fun readUntil(
        reader: BufferedReader,
        stop: String,
        saveContent: Boolean = true,
        trimLines: Boolean = true
    ): String {
        val builder = StringBuilder()
        var b: Int
        while (reader.read().also { b = it } != -1) {
            if (!trimLines || b.toChar() != '\n') {
                builder.append(b.toChar())
            }

            if (!saveContent && builder.length > stop.length) {
                builder.deleteAt(0)
            }

            if (builder.endsWith(stop)) {
                return builder.toString().dropLast(stop.length)
            }
        }
        return builder.toString()
    }

    private val keyTerminals = listOf(
        ' ', '\n', '/', '[', '(', '<'
    )

    private fun parseProperties(stream: BufferedReader): List<String> {
        // Properties are a list af key value pairs
        // Keys start with / and will have a value after them (which may be a value, nested properties, or an array)
        // There does not need to be a space between the key and the value if the value starts with a /, [, <<, or (
        // Properties end with >>

        val properties = mutableMapOf<String, String>()
        val builder = StringBuilder()
        var b: Int
        var key: String? = null
        var isReadingKey = false
        while (stream.read().also { b = it } != -1) {
            builder.append(b.toChar())

            // If << is encountered, parse properties
            if (key != null && builder.toString().endsWith("<<")) {
                val props = parseProperties(stream)
                properties[key] = props.joinToString(" ")
                builder.clear()
                key = null
            }

            // If >> is encountered, return
            if (builder.toString().endsWith(">>")) {
                if (key != null) {
                    properties[key] = builder.toString().dropLast(2).trim()
                }
                return properties.map { "${it.key} ${it.value}" }
            }

            // If / is encountered, parse key
            if (!isReadingKey && key == null && builder.toString().endsWith("/")) {
                builder.clear()
                builder.append("/")
                isReadingKey = true
            }

            // If it is reading a key and a terminal is encountered, start parsing value
            if (isReadingKey && b.toChar() in keyTerminals && builder.toString().trim().length > 1) {
                // Remove the terminal and trim whitespace
                key = builder.toString().dropLast(1).trim()

                // Start reading the value
                builder.clear()
                builder.append(b.toChar())
                isReadingKey = false
            }

            // TODO: Handle arrays properly
            val str = builder.toString().trim()
            val arrayStartCount = str.count { it == '[' }
            val arrayEndCount = str.count { it == ']' }
            val parenStartCount = str.count { it == '(' }
            val parenEndCount = str.count { it == ')' }
            if (key != null && b.toChar() == '/' && str.trim().length > 1 && arrayStartCount == arrayEndCount && parenStartCount == parenEndCount) {
                val value = builder.toString().dropLast(1).trim()
                properties[key] = value
                // A new key is starting
                builder.clear()
                builder.append(b.toChar())
                isReadingKey = true
                key = null
            }

        }

        if (key != null) {
            properties[key] = builder.toString().trim()
        }

        return properties.map { "${it.key} ${it.value}" }
    }

}
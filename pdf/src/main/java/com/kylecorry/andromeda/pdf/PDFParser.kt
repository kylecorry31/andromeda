package com.kylecorry.andromeda.pdf

import com.kylecorry.andromeda.core.io.forEachChar
import com.kylecorry.andromeda.core.io.readUntil
import com.kylecorry.andromeda.core.text.areBracketsBalanced
import java.io.BufferedReader
import java.io.InputStream

internal class PDFParser {

    private val keyTerminals = listOf(
        ' ', '\n', '/', '[', '(', '<'
    )

    fun parse(pdf: InputStream, ignoreStreams: Boolean = false): List<PDFObject> {
        val reader = pdf.bufferedReader()
        val objects = mutableListOf<PDFObject>()
        val builder = StringBuilder()
        reader.forEachChar { char ->
            builder.append(char)

            // Reset the builder if a new line is encountered
            if (char == '\n') {
                builder.clear()
            }

            // Object start
            if (builder.endsWith("obj")) {
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

        stream.forEachChar { char ->
            builder.append(char)

            // Parameters start
            if (builder.endsWith("<<")) {
                builder.clear()
                properties.addAll(parseParameters(stream))
            }

            // Stream start
            if (builder.endsWith("stream")) {
                builder.clear()
                streams.add(parseStream(stream, shouldParseStreams).toByteArray())
            }

            // Object end
            if (builder.endsWith("endobj")) {
                return PDFObject(id, properties, if (shouldParseStreams) streams else emptyList())
            }
        }

        return PDFObject(id, properties, if (shouldParseStreams) streams else emptyList())
    }

    private fun parseStream(reader: BufferedReader, shouldParse: Boolean): String {
        return reader.readUntil("endstream", shouldParse, trimLines = true)
    }

    private fun parseParameters(stream: BufferedReader): List<String> {
        val parameters = mutableMapOf<String, String>()
        val builder = StringBuilder()
        var key: String? = null
        var isReadingKey = false
        stream.forEachChar { char ->
            builder.append(char)

            // Nested parameters
            if (key != null && builder.endsWith("<<")) {
                val props = parseParameters(stream)
                // TODO: Add proper support for nested parameters
                parameters[key!!] = props.joinToString(" ")
                builder.clear()
                key = null
            }

            // End of parameters
            if (builder.endsWith(">>")) {
                if (key != null) {
                    parameters[key!!] = builder.toString().dropLast(2).trim()
                }
                // TODO: Add proper support for parameters
                return parameters.map { "${it.key} ${it.value}" }
            }

            // Key start
            if (!isReadingKey && key == null && builder.endsWith("/")) {
                builder.clear()
                builder.append("/")
                isReadingKey = true
            }

            // Key end
            if (isReadingKey && char in keyTerminals && builder.toString().trim().length > 1) {
                // Remove the terminal and trim whitespace
                key = builder.toString().dropLast(1).trim()

                // Start reading the value
                builder.clear()
                builder.append(char)
                isReadingKey = false
            }

            // Value end (encountered a new key)
            // TODO: Handle arrays and strings? (the values with parens) properly
            val str = builder.toString().trim()
            val arraysAreBalanced = str.areBracketsBalanced('[', ']')
            val parensAreBalanced = str.areBracketsBalanced('(', ')')
            if (key != null && char == '/' && str.trim().length > 1 && arraysAreBalanced && parensAreBalanced) {
                val value = builder.toString().dropLast(1).trim()
                parameters[key!!] = value
                // Get ready for a new key
                builder.clear()
                builder.append(char)
                isReadingKey = true
                key = null
            }

        }

        // The file ended before the parameters ended
        if (key != null) {
            parameters[key!!] = builder.toString().trim()
        }

        // TODO: Add proper support for parameters
        return parameters.map { "${it.key} ${it.value}" }
    }

}
package com.kylecorry.andromeda.pdf

import com.kylecorry.andromeda.compression.Zlib
import com.kylecorry.andromeda.core.io.forEachByte
import com.kylecorry.andromeda.core.io.readBytesUntil
import com.kylecorry.andromeda.core.text.areBracketsBalanced
import com.kylecorry.andromeda.core.toIntCompat
import java.io.InputStream

internal class PDFParser {

    private val keyTerminals = listOf(
        ' ', '\n', '/', '[', '(', '<'
    )

    fun parse(pdf: InputStream, ignoreStreams: Boolean = false): List<PDFObject> {
        val objects = mutableListOf<PDFObject>()
        val builder = StringBuilder()
        pdf.forEachByte { byte ->
            val char = byte.toChar()
            builder.append(char)

            // Reset the builder if a new line is encountered
            if (char == '\n') {
                builder.clear()
            }

            // Object start
            if (builder.endsWith("obj")) {
                objects.addAll(parseObject(builder.toString(), pdf, !ignoreStreams))
                builder.clear()
            }
        }
        return objects
    }

    private fun parseObject(
        startToken: String,
        stream: InputStream,
        shouldParseStreams: Boolean
    ): List<PDFObject> {
        val properties = mutableListOf<String>()
        val streams = mutableListOf<ByteArray>()
        val subObjects = mutableListOf<PDFObject>()
        val id = startToken.replace("obj", "").trim()
        val builder = StringBuilder()

        var isObjectStream = false
        var isStreamCompressed = false

        stream.forEachByte { byte ->
            val char = byte.toChar()
            builder.append(char)

            // Parameters start
            if (builder.endsWith("<<")) {
                builder.clear()
                properties.addAll(parseParameters(stream))
            }

            if (properties.contains("/Type /ObjStm")) {
                isObjectStream = true
            }

            if (properties.contains("/Filter /FlateDecode")) {
                isStreamCompressed = true
            }

            // Stream start
            if (builder.endsWith("stream")) {
                builder.clear()
                val s = parseStream(stream, shouldParseStreams || isObjectStream)
                if (isObjectStream) {
                    try {
                        val decoded = if (isStreamCompressed) {
                            Zlib.decompress(s)
                        } else {
                            s
                        }

                        val first = properties.firstOrNull { it.startsWith("/First") }?.let {
                            getDictionaryValue(it)?.toIntCompat()
                        } ?: 0

                        val objectIndices = decoded
                            .copyOfRange(0, first)
                            .decodeToString()
                            .split(' ')
                            .mapNotNull { it.toIntCompat() }
                            .chunked(2)
                            .map { it[0] to it[1] } + listOf(-1 to decoded.size - first)

                        val objects = objectIndices.zipWithNext()
                            .map {
                                val start = it.first.second + first
                                val end = it.second.second + first
                                PDFObject(
                                    "${it.first.first} 0",
                                    properties = parseParameters(
                                        decoded.copyOfRange(start, end).inputStream()
                                    ),
                                    streams = emptyList()
                                )
                            }

                        subObjects.addAll(objects)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                    }
                }
                if (shouldParseStreams) {
                    streams.add(s)
                }
            }

            // Object end
            if (builder.endsWith("endobj")) {
                return listOf(
                    PDFObject(
                        id,
                        properties,
                        if (shouldParseStreams) streams else emptyList()
                    )
                ) + subObjects
            }
        }

        return listOf(
            PDFObject(
                id,
                properties,
                if (shouldParseStreams) streams else emptyList()
            )
        ) + subObjects
    }

    private fun parseStream(reader: InputStream, shouldParse: Boolean): ByteArray {
        return reader.readBytesUntil(
            "endstream".toByteArray(),
            shouldParse,
            trimLines = true,
            trimStartAndEndOnly = true
        )
    }

    private fun parseParameters(stream: InputStream): List<String> {
        val parameters = mutableMapOf<String, String>()
        val builder = StringBuilder()
        var key: String? = null
        var isReadingKey = false
        stream.forEachByte { byte ->
            val char = byte.toChar()
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

    private fun getDictionaryValue(keyValuePair: String): String? {
        val firstSpace = keyValuePair.indexOf(' ')
        if (firstSpace == -1) {
            return null
        }
        return keyValuePair.substring(firstSpace + 1).trim()
    }

}
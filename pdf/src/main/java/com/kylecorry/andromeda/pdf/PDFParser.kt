package com.kylecorry.andromeda.pdf

import com.kylecorry.andromeda.compression.Zlib
import com.kylecorry.andromeda.core.io.forEachByte
import com.kylecorry.andromeda.core.io.readBytesUntil
import com.kylecorry.andromeda.core.text.areBracketsBalanced
import com.kylecorry.andromeda.core.toIntCompat
import com.kylecorry.sol.math.Range
import java.io.InputStream


internal class PDFParser {

    private val keyTerminals = listOf(
        ' ', '\n', '/', '[', '(', '<'
    )

    private val streamStarts = listOf(
        "stream\n",
        "stream\r",
        "stream\r\n"
    )

    fun parse(pdf: InputStream, ignoreStreams: Boolean = false): List<PDFValue.PDFObject> {
        val stream = pdf.buffered()
        val objects = mutableListOf<PDFValue.PDFObject>()
        val builder = StringBuilder()
        stream.forEachByte { byte ->
            val char = byte.toChar()
            builder.append(char)

            // Reset the builder if a new line is encountered
            if (char == '\n') {
                builder.clear()
            }

            // Object start
            if (builder.endsWith("obj")) {
                objects.addAll(parseObject(builder.toString(), stream, !ignoreStreams))
                builder.clear()
            }
        }
        return objects
    }

    private fun parseObject(
        startToken: String,
        stream: InputStream,
        shouldParseStreams: Boolean
    ): List<PDFValue.PDFObject> {
        val content = mutableListOf<PDFValue>()
        val subObjects = mutableListOf<PDFValue.PDFObject>()
        val splitId = startToken.replace("obj", "").trim().split(' ')
        val id = splitId.firstOrNull()?.toIntCompat() ?: -1
        val generation = splitId.getOrNull(1)?.toIntCompat() ?: 0
        val builder = StringBuilder()

        stream.forEachByte { byte ->
            val char = byte.toChar()
            builder.append(char)

            // Stream start
            if (streamStarts.any { builder.endsWith(it) }) {
                val before = builder.toString().substringBefore("stream").trim()
                if (before.isNotEmpty()) {
                    content.addAll(parseValues(before))
                }
                builder.clear()
                val properties =
                    content.lastOrNull { it is PDFValue.PDFDictionary } as? PDFValue.PDFDictionary

                val isObjectStream = properties?.get("/Type") == PDFValue.PDFName("/ObjStm")
                val isStreamCompressed =
                    properties?.get("/Filter") == PDFValue.PDFName("/FlateDecode")
                val s = parseStream(stream, shouldParseStreams || isObjectStream)
                if (isObjectStream) {
                    try {
                        val decoded = if (isStreamCompressed) {
                            Zlib.decompress(s)
                        } else {
                            s
                        }

                        val first =
                            (properties["/First"] as? PDFValue.PDFNumber)?.value?.toInt() ?: 0

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
                                PDFValue.PDFObject(
                                    it.first.first,
                                    0,
                                    content = parseValues(
                                        decoded.copyOfRange(start, end).decodeToString()
                                    )
                                )
                            }

                        subObjects.addAll(objects)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                    }
                }
                if (shouldParseStreams) {
                    content.add(PDFValue.PDFStream(s))
                }
            }

            // Object end
            if (builder.endsWith("endobj")) {
                val before = builder.toString().substringBefore("endobj").trim()
                if (before.isNotEmpty()) {
                    content.addAll(parseValues(before))
                }
                return listOf(
                    PDFValue.PDFObject(
                        id,
                        generation,
                        content
                    )
                ) + subObjects
            }
        }

        return listOf(
            PDFValue.PDFObject(
                id,
                generation,
                content
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

    private fun parseDictionary(
        // TODO: Operate on a string instead
        stream: InputStream,
        originalHasDictionaryStarted: Boolean = true
    ): PDFValue {
        var hasDictionaryStarted = originalHasDictionaryStarted
        val properties = mutableMapOf<PDFValue.PDFName, PDFValue>()
        val builder = StringBuilder()
        var key: String? = null
        var isReadingKey = false
        stream.forEachByte { byte ->
            val char = byte.toChar()
            builder.append(char)

            // Nested dictionary
            if (key != null && builder.endsWith("<<")) {
                properties[PDFValue.PDFName(key)] = parseDictionary(stream)
                builder.clear()
                key = null
            }

            // End of parameters
            if (builder.endsWith(">>")) {
                if (key != null) {
                    properties[PDFValue.PDFName(key)] =
                        parseValues(builder.toString().dropLast(2)).first()
                }
                return PDFValue.PDFDictionary(properties)
            }

            // Key start
            if (!isReadingKey && key == null && builder.endsWith("/") && hasDictionaryStarted) {
                builder.clear()
                builder.append("/")
                isReadingKey = true
            }

            // Dictionary start
            if (!hasDictionaryStarted && builder.endsWith("<<")) {
                hasDictionaryStarted = true
                builder.clear()
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
            val str = builder.toString().trim()
            val arraysAreBalanced = str.areBracketsBalanced('[', ']')
            val parensAreBalanced = str.areBracketsBalanced('(', ')')
            if (key != null && char == '/' && str.trim().length > 1 && arraysAreBalanced && parensAreBalanced) {
                val value = builder.toString().dropLast(1)
                properties[PDFValue.PDFName(key)] = parseValues(value).first()
                // Get ready for a new key
                builder.clear()
                builder.append(char)
                isReadingKey = true
                key = null
            }

        }

        // The file ended before the parameters ended
        if (key != null) {
            properties[PDFValue.PDFName(key)] = parseValues(builder.toString()).first()
        }

        if (!hasDictionaryStarted) {
            return parseValues(builder.toString()).firstOrNull()
                ?: PDFValue.PDFDictionary(emptyMap())
        }

        return PDFValue.PDFDictionary(properties)
    }

    private fun parseNextValue(value: String): Pair<PDFValue?, String> {
        val indirectObjectRegex = Regex("""^(\d+)\s+(\d+)?\s+R""")
        val trimmed = value.trim()
        return if (trimmed.startsWith("<<")) {
            parseDictionaryValue(trimmed)
        } else if (trimmed.startsWith('(')) {
            parseStringValue(trimmed, '(', ')')
        } else if (trimmed.startsWith('<')) {
            parseStringValue(trimmed, '<', '>')
        } else if (trimmed.startsWith('[')) {
            parseArrayValue(trimmed)
        } else if (indirectObjectRegex.containsMatchIn(trimmed)) {
            val match = indirectObjectRegex.find(trimmed) ?: return null to trimmed
            val id = match.groupValues[1].toIntOrNull() ?: return null to trimmed
            val generation = match.groupValues.getOrNull(2)?.toIntOrNull() ?: 0
            PDFValue.PDFIndirectObject(id, generation) to trimmed.drop(match.range.last + 1)
        } else if (trimmed.startsWith('/')) {
            val token = trimmed.substringBefore(' ')
            PDFValue.PDFName(token) to trimmed.drop(token.length)
        } else if (trimmed == "null") {
            PDFValue.PDFNull() to trimmed.drop(4)
        } else if (trimmed == "true" || trimmed == "false") {
            PDFValue.PDFBoolean(trimmed.toBoolean()) to trimmed.drop(if (trimmed == "true") 4 else 5)
                .trim()
        } else {
            val nextToken = trimmed.takeWhile { it.isLetterOrDigit() || it == '.' || it == '-' }
            if (nextToken.isNotEmpty()) {
                val number = nextToken.toDoubleOrNull()
                if (number != null) {
                    PDFValue.PDFNumber(number) to trimmed.drop(nextToken.length)
                } else {
                    null to trimmed // If it can't be parsed, return the original string
                }
            } else {
                null to trimmed // If no token is found, return the original string
            }
        }
    }

    private fun parseStringValue(value: String, start: Char, end: Char): Pair<PDFValue, String> {
        val stringRange = getRange(value, start, end)
            ?: return PDFValue.PDFString(value) to value
        val str = value.substring(stringRange.start, stringRange.end)
        val remaining = value.substring(stringRange.end)
        val content = str.drop(1).dropLast(1)
        return PDFValue.PDFString(content) to remaining
    }

    private fun parseArrayValue(value: String): Pair<PDFValue?, String> {
        val arrayRange = getRange(value, '[', ']') ?: return null to value
        val str = value.substring(arrayRange.start, arrayRange.end)
        val remaining = value.substring(arrayRange.end)
        val content = str.drop(1).dropLast(1)
        return PDFValue.PDFArray(parseValues(content)) to remaining
    }

    private fun parseDictionaryValue(value: String): Pair<PDFValue?, String> {
        val range = getRange(value, '<', '>', 2) ?: return null to value
        val str = value.substring(range.start, range.end)
        val remaining = value.substring(range.end)
        return parseDictionary(str.byteInputStream(), false) to remaining
    }

    private fun getRange(text: String, start: Char, end: Char, count: Int = 1): Range<Int>? {
        val builder = StringBuilder()
        var startCount = 0
        var endCount = 0
        var startIndex: Int? = null
        var endIndex: Int? = null
        for (char in text) {
            builder.append(char)
            if (char == start) {
                startCount++
                if (startIndex == null && startCount == count) {
                    startIndex = builder.length - count
                }
            } else if (char == end) {
                endCount++
                if (endCount == startCount && startCount >= count) {
                    endIndex = builder.length
                    break
                }
            }
        }

        if (startIndex == null || endIndex == null) {
            return null
        }

        return Range(startIndex, endIndex)
    }

    private fun parseValues(value: String): List<PDFValue> {
        val values = mutableListOf<PDFValue>()
        var remaining = value
        var lastRemaining = ""
        while (remaining.isNotEmpty() && remaining != lastRemaining) {
            lastRemaining = remaining
            val (parsedValue, rest) = parseNextValue(remaining)
            if (parsedValue != null) {
                values.add(parsedValue)
            }
            remaining = rest.trim()
        }

        if (remaining.isNotBlank()) {
            values.add(PDFValue.PDFString(remaining.trim()))
        }

        return values
    }
}
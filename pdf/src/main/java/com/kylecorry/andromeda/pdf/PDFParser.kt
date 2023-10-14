package com.kylecorry.andromeda.pdf

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

internal class PDFParser {

    fun parse(pdf: InputStream, ignoreStreams: Boolean = false): List<PDFObject> {
        val properties = mutableMapOf<String, MutableList<String>>()
        val streams = mutableMapOf<String, MutableList<String>>()
        var lastObject: String? = null
        var streamIndex = -1
        var inStream = false
        val objectStart = Regex("(\\d+ \\d+) obj")
        val objectEnd = Regex("endobj")
        val streamStart = Regex("stream")
        val streamEnd = Regex("endstream")
        BufferedReader(InputStreamReader(pdf)).use { reader ->
            while (true) {
                var line = reader.readLine() ?: break
                if (line.matches(objectStart)) {
                    val matches = objectStart.find(line) ?: continue
                    val key = matches.groupValues[1]
                    properties[key] = mutableListOf()
                    lastObject = key
                    streamIndex = -1
                    inStream = false
                    continue
                }

                if (line.matches(objectEnd)) {
                    lastObject = null
                    inStream = false
                    continue
                }

                var justFoundStreamStart = false

                if (!inStream && line.contains(streamStart)) {
                    inStream = true
                    streamIndex++
                    justFoundStreamStart = true
                    line = line.replace(streamStart, "")
                }

                if (line.matches(streamEnd)) {
                    inStream = false
                    continue
                }

                val propertyStartIdx = line.indexOf("<<")
                val propertyEndIdx = line.indexOf(">>")

                val hasPropertiesStart = propertyStartIdx != -1
                val hasPropertiesEnd = propertyEndIdx != -1 && propertyEndIdx > propertyStartIdx
                val arePropertiesSingleLine = hasPropertiesStart && hasPropertiesEnd
                if ((hasPropertiesStart || hasPropertiesEnd) && !arePropertiesSingleLine) {
                    continue
                }

                if (!inStream || justFoundStreamStart) {
                    if (line.isBlank()) {
                        continue
                    }

                    lastObject?.also {
                        if (arePropertiesSingleLine) {
                            properties[it]?.addAll(parseSingleLineProperties(line.trim()))
                        } else {
                            properties[it]?.add(line.trim())
                        }
                    }
                }

                // TODO: Treat streams as byte arrays
                if (!ignoreStreams && inStream && !justFoundStreamStart) {
                    lastObject?.let {
                        if (!streams.containsKey(it)) {
                            streams[it] = mutableListOf()
                        }

                        if (streams[it]!!.size <= streamIndex) {
                            streams[it]!!.add("")
                        }

                        streams[it]!![streamIndex] += line
                    }
                }
            }
        }

        val objects = mutableListOf<PDFObject>()
        for (key in properties.keys) {
            objects.add(
                PDFObject(
                    key,
                    properties[key]!!,
                    streams[key]?.map { it.toByteArray() } ?: emptyList()))
        }

        return objects.sortedBy { it.id }
    }

    private fun parseSingleLineProperties(line: String): List<String> {
        val properties = mutableListOf<String>()
        val allTokens = line
            .replace("/", " /")
            .replace("<<", " << ")
            .replace(">>", " >> ")
            .replace("[", " [")
            .replace("]", "] ")
            .replace(Regex("\\[\\s+"), "[")
            .trim()
            .split(Regex("\\s+"))
        val tokens = allTokens.filter { it.isNotBlank() }.drop(1).dropLast(1)
        if (tokens.isEmpty()) {
            return emptyList()
        }
        var currentProperty = tokens.first()
        var subPropertyCount = 0
        for (token in tokens.drop(1)) {
            if (token.startsWith("/") && subPropertyCount >= 1 && areArraysClosed(currentProperty)) {
                properties.add(currentProperty)
                currentProperty = token
                subPropertyCount = 0
            } else {
                currentProperty += " $token"
                subPropertyCount++
            }
        }

        properties.add(currentProperty)
        return properties
    }

    private fun areArraysClosed(text: String): Boolean {
        return isClosed(text, '[', ']') && isClosed(text, '<', '>')
    }

    /**
     * Determines if a string has matching open and close characters.
     */
    private fun isClosed(text: String, open: Char, close: Char): Boolean {
        var openCount = 0
        var closeCount = 0
        for (c in text) {
            if (c == open) {
                openCount++
            } else if (c == close) {
                closeCount++
            }
        }

        return openCount == closeCount
    }

}
package com.kylecorry.andromeda.pdf

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class PDFParser {

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
                val line = reader.readLine() ?: break
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

                if (line.matches(streamStart)) {
                    inStream = true
                    streamIndex++
                    continue
                }

                if (line.matches(streamEnd)) {
                    inStream = false
                    continue
                }

                val hasPropertiesStart = line.startsWith("<<")
                val hasPropertiesEnd = line.endsWith(">>")
                val arePropertiesSingleLine = hasPropertiesStart && hasPropertiesEnd
                if ((hasPropertiesStart || hasPropertiesEnd) && !arePropertiesSingleLine) {
                    continue
                }

                if (!inStream) {
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

                if (!ignoreStreams && inStream) {
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
            objects.add(PDFObject(key, properties[key]!!, streams[key] ?: emptyList()))
        }

        return objects.sortedBy { it.id }
    }

    private fun parseSingleLineProperties(line: String): List<String> {
        val properties = mutableListOf<String>()
        val allTokens = line.trim().split(Regex("\\s+"))
        val tokens = allTokens.drop(1).dropLast(1).filter { it.isNotBlank() }
        if (tokens.isEmpty()) {
            return emptyList()
        }
        var currentProperty = tokens.first()
        var subPropertyCount = 0
        for (token in tokens.drop(1)) {
            if (token.startsWith("/") && subPropertyCount >= 1) {
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

}
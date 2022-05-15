package com.kylecorry.andromeda.wkt

import java.io.InputStream

object CRSWellKnownTextConvert {

    fun toWKT(data: String): WKTValue? {
        if (data.isEmpty()) {
            return null
        }

        if (data[0] == '"') {
            return readNextString(data).first
        }

        if (isNumber(data[0])) {
            return readNextNumber(data).first
        }

        return readNextSection(data).first
    }

    fun toWKT(stream: InputStream): WKTValue? {
        // TODO: Make the parser more memory efficient
        return toWKT(stream.reader().readText())
    }

    private fun readNextSection(data: String): Pair<WKTValue, String> {
        val startIdx = data.indexOf("[")
        val endIdx1 = data.indexOf("]")
        val endIdx2 = data.indexOf(",")

        if (startIdx == -1 && endIdx1 == -1) {
            return WKTSection("", emptyList()) to data
        }

        if ((startIdx == -1 && endIdx1 != -1) || endIdx1 < startIdx) {
            return WKTSection(
                data.substring(0, endIdx1),
                emptyList()
            ) to data.substring(endIdx1 + 1)
        }

        if (endIdx2 < startIdx && endIdx2 != -1) {
            return WKTSection(
                data.substring(0, endIdx2),
                emptyList()
            ) to data.substring(endIdx2 + 1)
        }

        val name = data.substring(0, startIdx)

        val wkt = mutableListOf<WKTValue>()

        var text = data.substring(startIdx + 1)

        while (text.isNotEmpty()) {
            if (text[0] == '"') {
                val value = readNextString(text)
                wkt.add(value.first)
                text = value.second
            } else if (isNumber(text[0])) {
                val value = readNextNumber(text)
                wkt.add(value.first)
                text = value.second
            } else if (text[0].isLetter()) {
                val value = readNextSection(text)
                wkt.add(value.first)
                text = value.second
            } else if (text[0] == ']') {
                break
            } else {
                text = text.substring(1)
            }
        }

        if (text.isEmpty()) {
            return WKTSection(name, wkt) to text
        }

        return WKTSection(name, wkt) to text.substring(1)
    }

    fun fromWKT(wkt: WKTValue): String {
        return when (wkt) {
            is WKTString -> fromWKTString(wkt)
            is WKTNumber -> fromWKTNumber(wkt)
            is WKTSection -> fromWKTSection(wkt)
            else -> ""
        }
    }

    // Assumes it starts on a "
    private fun readNextString(text: String): Pair<WKTValue, String> {
        val nextIdx = text.indexOf('"', 1)
        if (nextIdx != -1) {
            return WKTString(text.substring(1, nextIdx)) to text.substring(nextIdx + 1)
        }
        return WKTString("") to text.substring(1)
    }

    private fun readNextNumber(text: String): Pair<WKTValue, String> {
        var idx = 0
        while (idx <= text.lastIndex && isNumber(text[idx])) {
            idx++
        }
        return WKTNumber(text.substring(0, idx).toDouble()) to text.substring(idx)
    }

    private fun isNumber(char: Char): Boolean {
        return char.isDigit() || char == '.' || char == '-'
    }

    private fun fromWKTString(wkt: WKTString): String {
        return "\"${wkt.value}\""
    }

    private fun fromWKTNumber(wkt: WKTNumber): String {
        return wkt.value.toString()
    }

    private fun fromWKTSection(wkt: WKTSection): String {
        return "${wkt.name}[${wkt.values.joinToString(",") { fromWKT(it) }}]"
    }


}
package com.kylecorry.andromeda.pdf

data class PDFObject(val id: String, val properties: List<String>, val streams: List<ByteArray>) {

    operator fun get(property: String): String? {
        val prop =
            properties.firstOrNull { it.startsWith(property, ignoreCase = true) } ?: return null
        return prop.substring(property.length).trim()
    }

    fun getArray(property: String): List<String> {
        val value = get(property) ?: return emptyList()
        val matches = arrayRegex.find(value) ?: return emptyList()
        if (matches.groupValues.size < 2) {
            return emptyList()
        }

        return matches.groupValues[1].split(" ").filter { it.isNotBlank() }
    }

    companion object {
        private val arrayRegex = Regex("\\[(.*)]")
    }

}

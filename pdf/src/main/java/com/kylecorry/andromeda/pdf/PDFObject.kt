package com.kylecorry.andromeda.pdf

data class PDFObject(val id: String, val properties: List<String>, val streams: List<String>) {

    operator fun get(property: String): String? {
        val prop =
            properties.firstOrNull { it.startsWith(property, ignoreCase = true) } ?: return null
        return prop.substring(property.length).trim()
    }

}

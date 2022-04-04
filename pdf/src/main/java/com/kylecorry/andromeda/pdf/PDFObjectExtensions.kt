package com.kylecorry.andromeda.pdf

private val idRegex = Regex("(\\d+ \\d+)")

fun List<PDFObject>.getById(id: String): PDFObject? {
    val matches = idRegex.find(id) ?: return null
    if (matches.groupValues.size >= 2) {
        return firstOrNull { it.id == matches.groupValues[1] }
    }
    return null
}

fun List<PDFObject>.getByProperty(
    key: String,
    value: String
): List<PDFObject> {
    return filter {
        value.contentEquals(it[key], true)
    }
}
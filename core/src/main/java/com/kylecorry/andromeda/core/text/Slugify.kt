package com.kylecorry.andromeda.core.text

import java.text.Normalizer


private val invalidChars = Regex("[^a-z0-9\\s-]")
private val multipleSpaces = Regex("\\s+")
private val whitespace = Regex("\\s")
private val nonSpacingMark = "\\p{Mn}+".toRegex()

fun String.slugify(): String {
    return this
        .removeAccents()
        .lowercase()
        .replace(invalidChars, "")
        .replace(multipleSpaces, " ")
        .trim()
        .replace(whitespace, "-")
}

private fun String.removeAccents(): String {
    // Adapted from https://stackoverflow.com/questions/51731574/removing-accents-and-diacritics-in-kotlin
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(nonSpacingMark, "")
}

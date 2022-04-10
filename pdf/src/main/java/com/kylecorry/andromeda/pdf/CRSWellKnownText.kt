package com.kylecorry.andromeda.pdf

interface WKTValue
data class WKTString(val value: String) : WKTValue
data class WKTNumber(val value: Double) : WKTValue
data class WKTSection(val name: String, val values: List<WKTValue>) : WKTValue {
    inline fun <reified T : WKTValue> get(idx: Int): T? {
        val value = values.getOrNull(idx)
        if (value is T) {
            return value
        }
        return null
    }
}

fun WKTValue.getSection(name: String): WKTSection? {
    if (this is WKTSection) {
        if (name.equals(name, true)) {
            return this
        }

        for (value in values) {
            val section = value.getSection(name)
            if (section != null) {
                return section
            }
        }
    }
    return null
}
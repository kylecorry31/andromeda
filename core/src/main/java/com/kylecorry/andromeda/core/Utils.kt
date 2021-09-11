package com.kylecorry.andromeda.core

import android.os.Bundle
import android.util.Range
import com.kylecorry.andromeda.core.specifications.Specification
import java.util.*

fun tryOrNothing(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
    }
}

fun <T : Comparable<T>> List<T>.rangeOrNull(): Range<T>? {
    val min = minOrNull() ?: return null
    val max = maxOrNull() ?: return null
    return Range(min, max)
}

fun <T> List<T>.filterSatisfied(spec: Specification<T>): List<T> {
    return filter { spec.isSatisfiedBy(it) }
}

fun <T> List<T>.filterNotSatisfied(spec: Specification<T>): List<T> {
    return filterNot { spec.isSatisfiedBy(it) }
}

fun <T> List<T>.filterIndices(indices: List<Int>): List<T> {
    return filterIndexed { index, _ -> indices.contains(index) }
}

fun String.capitalizeCompat(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

fun String.capitalizeWords(): String {
    val words = split(" ")
    return words.joinToString(" ") { it.capitalizeCompat() }
}


fun String.toDoubleCompat(): Double? {
    val asDouble = try {
        this.replace(",", ".").toDoubleOrNull()
    } catch (e: Exception) {
        null
    }
    asDouble ?: return null
    if (asDouble.isNaN() || asDouble.isInfinite()) {
        return null
    }
    return asDouble
}

fun String.toFloatCompat(): Float? {
    val asFloat = try {
        this.replace(",", ".").toFloatOrNull()
    } catch (e: Exception) {
        null
    }
    asFloat ?: return null
    if (asFloat.isNaN() || asFloat.isInfinite()) {
        return null
    }
    return asFloat
}

fun String.toIntCompat(): Int? {
    return try {
        this.replace(",", ".").toIntOrNull()
    } catch (e: Exception) {
        null
    }
}

fun String.toLongCompat(): Long? {
    return try {
        this.replace(",", ".").toLongOrNull()
    } catch (e: Exception) {
        null
    }
}

fun Bundle.toMap(): Map<String, Any?> {
    val keys = keySet()
    val map = mutableMapOf<String, Any?>()
    for (key in keys) {
        map[key] = get(key)
    }
    return map
}
package com.kylecorry.andromeda.core

import android.os.Bundle
import android.util.Range
import com.kylecorry.luna.specifications.Specification

inline fun tryOrNothing(block: () -> Unit) {
    try {
        block()
    } catch (_: Exception) {
    }
}

inline fun tryOrLog(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

inline fun <T> tryOrDefault(default: T, block: () -> T): T {
    return try {
        block()
    } catch (e: Exception) {
        default
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


fun Bundle.toMap(): Map<String, Any?> {
    val keys = keySet()
    val map = mutableMapOf<String, Any?>()
    for (key in keys) {
        @Suppress("DEPRECATION")
        map[key] = get(key)
    }
    return map
}

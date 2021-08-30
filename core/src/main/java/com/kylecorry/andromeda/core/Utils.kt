package com.kylecorry.andromeda.core

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
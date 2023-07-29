package com.kylecorry.andromeda.preferences

fun <K, V> Map<K, V>.changes(other: Map<K, V>): Collection<K> {
    val changes = mutableSetOf<K>()

    // Key added or value changed
    for (key in keys) {
        if (other[key] != this[key]) {
            changes.add(key)
        }
    }

    // Key removed
    for (key in other.keys) {
        if (!containsKey(key)) {
            changes.add(key)
        }
    }

    return changes
}
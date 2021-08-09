package com.kylecorry.andromeda.preferences

import kotlin.reflect.KProperty

class IntEnumPreference<T>(
    private val preferences: Preferences,
    private val name: String,
    private val mappings: Map<Int, T>,
    private val defaultValue: T
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val raw = preferences.getInt(name) ?: return defaultValue
        return mappings.getOrDefault(raw, defaultValue)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val key = mappings.entries.firstOrNull { it.value == value }?.key ?: return
        preferences.putInt(name, key)
    }

}
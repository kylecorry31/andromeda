package com.kylecorry.andromeda.preferences

import kotlin.reflect.KProperty

class StringEnumPreference<T>(
    private val preferences: IPreferences,
    private val name: String,
    private val mappings: Map<String, T>,
    private val defaultValue: T,
    private val saveDefault: Boolean = false
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val raw = preferences.getString(name)
        if (raw == null && saveDefault) {
            val defaultKey = mappings.entries.firstOrNull { it.value == defaultValue }?.key ?: return defaultValue
            preferences.putString(name, defaultKey)
            return defaultValue
        } else if (raw == null) {
            return defaultValue
        }
        return mappings.getOrDefault(raw, defaultValue)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val key = mappings.entries.firstOrNull { it.value == value }?.key ?: return
        preferences.putString(name, key)
    }

}
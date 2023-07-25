package com.kylecorry.andromeda.preferences

import kotlin.reflect.KProperty

class DoublePreference(
    private val preferences: IPreferences,
    private val name: String,
    private val defaultValue: Double,
    private val saveDefault: Boolean = false
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Double {
        val value = preferences.getDouble(name)
        if (value == null && saveDefault) {
            preferences.putDouble(name, defaultValue)
        }
        return value ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
        preferences.putDouble(name, value)
    }

}
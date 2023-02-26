package com.kylecorry.andromeda.preferences

import kotlin.reflect.KProperty

class DoublePreference(
    private val preferences: IPreferences,
    private val name: String,
    private val defaultValue: Double
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Double {
        return preferences.getDouble(name) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
        preferences.putDouble(name, value)
    }

}
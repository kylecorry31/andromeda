package com.kylecorry.andromeda.preferences

import kotlin.reflect.KProperty

class FloatPreference(
    private val preferences: Preferences,
    private val name: String,
    private val defaultValue: Float
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return preferences.getFloat(name) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        preferences.putFloat(name, value)
    }

}
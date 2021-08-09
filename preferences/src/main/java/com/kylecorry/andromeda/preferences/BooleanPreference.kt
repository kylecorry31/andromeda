package com.kylecorry.andromeda.preferences

import kotlin.reflect.KProperty

class BooleanPreference(
    private val preferences: Preferences,
    private val name: String,
    private val defaultValue: Boolean
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return preferences.getBoolean(name) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        preferences.putBoolean(name, value)
    }

}
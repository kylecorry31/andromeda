package com.kylecorry.andromeda.preferences

import kotlin.reflect.KProperty

class StringPreference(
    private val preferences: IPreferences,
    private val name: String,
    private val defaultValue: String,
    private val saveDefault: Boolean = false
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        val value = preferences.getString(name)
        if (value == null && saveDefault) {
            preferences.putString(name, defaultValue)
        }
        return value ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        preferences.putString(name, value)
    }

}
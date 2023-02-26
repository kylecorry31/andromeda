package com.kylecorry.andromeda.preferences

import kotlin.reflect.KProperty

class StringPreference(
    private val preferences: IPreferences,
    private val name: String,
    private val defaultValue: String
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return preferences.getString(name) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        preferences.putString(name, value)
    }

}
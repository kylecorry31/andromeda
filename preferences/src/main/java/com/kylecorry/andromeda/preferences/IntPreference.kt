package com.kylecorry.andromeda.preferences

import kotlin.reflect.KProperty

class IntPreference(
    private val preferences: IPreferences,
    private val name: String,
    private val defaultValue: Int
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return preferences.getInt(name) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        preferences.putInt(name, value)
    }

}
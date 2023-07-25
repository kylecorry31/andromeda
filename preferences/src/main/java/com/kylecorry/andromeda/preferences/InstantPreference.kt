package com.kylecorry.andromeda.preferences

import java.time.Instant
import kotlin.reflect.KProperty

class InstantPreference(
    private val preferences: IPreferences,
    private val name: String,
    private val defaultValue: Instant,
    private val saveDefault: Boolean = false
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Instant {
        val value = preferences.getInstant(name)
        if (value == null && saveDefault) {
            preferences.putInstant(name, defaultValue)
        }
        return value ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Instant) {
        preferences.putInstant(name, value)
    }

}
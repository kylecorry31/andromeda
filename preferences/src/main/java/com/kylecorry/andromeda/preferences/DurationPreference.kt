package com.kylecorry.andromeda.preferences

import java.time.Duration
import java.time.LocalDate
import kotlin.reflect.KProperty

class DurationPreference(
    private val preferences: IPreferences,
    private val name: String,
    private val defaultValue: Duration,
    private val saveDefault: Boolean = false
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Duration {
        val value = preferences.getDuration(name)
        if (value == null && saveDefault) {
            preferences.putDuration(name, defaultValue)
        }
        return value ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Duration) {
        preferences.putDuration(name, value)
    }

}
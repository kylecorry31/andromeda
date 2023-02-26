package com.kylecorry.andromeda.preferences

import java.time.Duration
import java.time.LocalDate
import kotlin.reflect.KProperty

class DurationPreference(
    private val preferences: IPreferences,
    private val name: String,
    private val defaultValue: Duration
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Duration {
        return preferences.getDuration(name) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Duration) {
        preferences.putDuration(name, value)
    }

}
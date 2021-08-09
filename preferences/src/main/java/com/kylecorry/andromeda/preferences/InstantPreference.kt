package com.kylecorry.andromeda.preferences

import java.time.Instant
import kotlin.reflect.KProperty

class InstantPreference(
    private val preferences: Preferences,
    private val name: String,
    private val defaultValue: Instant
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Instant {
        return preferences.getInstant(name) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Instant) {
        preferences.putInstant(name, value)
    }

}
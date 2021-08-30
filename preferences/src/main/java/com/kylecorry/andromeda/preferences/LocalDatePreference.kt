package com.kylecorry.andromeda.preferences

import java.time.LocalDate
import kotlin.reflect.KProperty

class LocalDatePreference(
    private val preferences: Preferences,
    private val name: String,
    private val defaultValue: LocalDate
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): LocalDate {
        return preferences.getLocalDate(name) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: LocalDate) {
        preferences.putLocalDate(name, value)
    }

}
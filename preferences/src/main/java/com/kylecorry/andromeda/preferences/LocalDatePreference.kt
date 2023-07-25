package com.kylecorry.andromeda.preferences

import java.time.LocalDate
import kotlin.reflect.KProperty

class LocalDatePreference(
    private val preferences: IPreferences,
    private val name: String,
    private val defaultValue: LocalDate,
    private val saveDefault: Boolean = false
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): LocalDate {
        val value = preferences.getLocalDate(name)
        if (value == null && saveDefault) {
            preferences.putLocalDate(name, defaultValue)
        }
        return value ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: LocalDate) {
        preferences.putLocalDate(name, value)
    }

}
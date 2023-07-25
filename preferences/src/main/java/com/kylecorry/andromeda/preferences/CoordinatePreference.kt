package com.kylecorry.andromeda.preferences

import com.kylecorry.sol.units.Coordinate
import kotlin.reflect.KProperty

class CoordinatePreference(
    private val preferences: IPreferences,
    private val name: String,
    private val defaultValue: Coordinate,
    private val saveDefault: Boolean = false
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Coordinate {
        val value = preferences.getCoordinate(name)
        if (value == null && saveDefault) {
            preferences.putCoordinate(name, defaultValue)
        }
        return value ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Coordinate) {
        preferences.putCoordinate(name, value)
    }

}
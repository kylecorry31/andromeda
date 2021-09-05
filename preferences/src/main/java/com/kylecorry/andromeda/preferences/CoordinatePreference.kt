package com.kylecorry.andromeda.preferences

import com.kylecorry.sol.units.Coordinate
import kotlin.reflect.KProperty

class CoordinatePreference(
    private val preferences: Preferences,
    private val name: String,
    private val defaultValue: Coordinate
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Coordinate {
        return preferences.getCoordinate(name) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Coordinate) {
        preferences.putCoordinate(name, value)
    }

}
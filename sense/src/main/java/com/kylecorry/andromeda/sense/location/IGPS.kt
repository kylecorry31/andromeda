package com.kylecorry.andromeda.sense.location

import com.kylecorry.andromeda.core.sensors.IAltimeter
import com.kylecorry.andromeda.core.sensors.IClock
import com.kylecorry.andromeda.core.sensors.ISensor
import com.kylecorry.andromeda.core.sensors.ISpeedometer
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate

interface IGPS: ISensor, IAltimeter, IClock, ISpeedometer {
    /**
     * The location
     */
    val location: Coordinate

    /**
     * The vertical accuracy in meters at the 68% confidence level
     */
    val verticalAccuracy: Float?

    /**
     * The horizontal accuracy in meters at the 68% confidence level
     */
    val horizontalAccuracy: Float?

    /**
     * The number of satellites used to calculate the location
     */
    val satellites: Int?

    /**
     * The altitude above MSL in meters
     */
    val mslAltitude: Float?

    /**
     * The bearing in degrees (True North)
     */
    val bearing: Bearing?

    /**
     * The bearing in degrees (True North)
     */
    val rawBearing: Float?

    /**
     * The bearing accuracy in degrees at the 68% confidence level
     */
    val bearingAccuracy: Float?

    /**
     * The speed accuracy in meters per second at the 68% confidence level
     */
    val speedAccuracy: Float?
}

fun IGPS.hasFix(): Boolean {
    if (!hasValidReading) {
        return false
    }

    // Satellites are only null when the device doesn't report them
    if (satellites == null) {
        return true
    }

    return (satellites ?: 0) >= 0
}
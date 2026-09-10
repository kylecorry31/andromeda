package com.kylecorry.andromeda.sense.location

import android.os.SystemClock
import com.kylecorry.andromeda.core.sensors.IAltimeter
import com.kylecorry.andromeda.core.sensors.IClock
import com.kylecorry.andromeda.core.sensors.ISensor
import com.kylecorry.andromeda.core.sensors.ISpeedometer
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import java.time.Duration

interface IGPS : ISensor, IAltimeter, IClock, ISpeedometer {
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

    /**
     * The time of the last fix in system elapsed time (nanoseconds)
     */
    val fixTimeElapsedNanos: Long?
}

/**
 * Determines if there is a recent location fix
 * @param maxFixAge the age at which a fix is considered lost
 */
fun IGPS.hasFix(maxFixAge: Duration = Duration.ofSeconds(30)): Boolean {
    if (!hasValidReading) {
        return false
    }

    val fixTime = fixTimeElapsedNanos ?: return false

    return SystemClock.elapsedRealtimeNanos() - fixTime <= maxFixAge.toNanos()
}
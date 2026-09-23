package com.kylecorry.andromeda.sense.v2.location

import com.kylecorry.andromeda.sense.v2.ISensorReading
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Speed
import java.time.Instant

data class LocationSensorReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val location: Coordinate,
    val horizontalAccuracy: Distance?,
    val altitude: Distance?,
    val verticalAccuracy: Distance?,
    val mslAltitude: Distance?,
    val mslAccuracy: Distance?,
    val bearing: Bearing?,
    val bearingAccuracy: Float?,
    val speed: Speed?,
    val speedAccuracy: Speed?
) : ISensorReading

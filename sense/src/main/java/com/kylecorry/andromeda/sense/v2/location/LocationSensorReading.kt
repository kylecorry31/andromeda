package com.kylecorry.andromeda.sense.v2.location

import com.kylecorry.andromeda.sense.v2.readings.AltitudeReading
import com.kylecorry.andromeda.sense.v2.readings.BearingReading
import com.kylecorry.andromeda.sense.v2.readings.EventTimeReading
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Speed
import java.time.Instant

data class LocationSensorReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val location: Coordinate,
    val locationAccuracy: Distance?,
    override val altitude: Distance?,
    val altitudeAccuracy: Distance?,
    val mslAltitude: Distance?,
    val mslAltitudeAccuracy: Distance?,
    override val bearing: Bearing?,
    val bearingAccuracy: Float?,
    val speed: Speed?,
    val speedAccuracy: Speed?
) : EventTimeReading, AltitudeReading, BearingReading

package com.kylecorry.andromeda.sense.v2.altitude

import com.kylecorry.andromeda.sense.v2.readings.AltitudeReading
import com.kylecorry.andromeda.sense.v2.readings.EventTimeReading
import com.kylecorry.sol.units.Distance
import java.time.Instant

data class FusedAltimeterReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    override val altitude: Distance
) : EventTimeReading, AltitudeReading

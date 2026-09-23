package com.kylecorry.andromeda.sense.v2.location

import com.kylecorry.andromeda.sense.location.Satellite
import com.kylecorry.andromeda.sense.v2.readings.EventTimeReading
import java.time.Instant

data class SatelliteStatusReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val satellites: Int?,
    val satelliteDetails: List<Satellite>?
) : EventTimeReading

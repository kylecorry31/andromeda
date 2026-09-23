package com.kylecorry.andromeda.sense.v2.orientation

import com.kylecorry.andromeda.sense.v2.readings.EventTimeReading
import java.time.Instant

data class DeviceOrientationReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val orientation: Orientation
) : EventTimeReading {
    enum class Orientation {
        Portrait,
        PortraitInverse,
        Flat,
        FlatInverse,
        Landscape,
        LandscapeInverse
    }
}

package com.kylecorry.andromeda.sense.v2.compass

import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.readings.BearingReading
import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import com.kylecorry.sol.units.Bearing
import java.time.Instant

data class CompassReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val rawBearing: Float,
    override val accuracy: SensorAccuracy
) : SensorAccuracyReading, BearingReading {
    override val bearing: Bearing
        get() = Bearing.from(rawBearing)
}

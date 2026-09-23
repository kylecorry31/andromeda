package com.kylecorry.andromeda.sense.v2.orientation

import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.sol.math.Euler
import java.time.Instant

data class OrientationReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val orientation: Euler,
    override val accuracy: SensorAccuracy
) : SensorAccuracyReading

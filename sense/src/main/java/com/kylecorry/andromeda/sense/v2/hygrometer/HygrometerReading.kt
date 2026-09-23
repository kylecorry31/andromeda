package com.kylecorry.andromeda.sense.v2.hygrometer

import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import java.time.Instant

data class HygrometerReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val humidity: Float,
    override val accuracy: SensorAccuracy
) : SensorAccuracyReading

package com.kylecorry.andromeda.sense.v2.light

import com.kylecorry.andromeda.sense.v2.ISensorEventReading
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import java.time.Instant

data class LightReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val illuminance: Float,
    override val accuracy: SensorAccuracy
) : ISensorEventReading

package com.kylecorry.andromeda.sense.v2.pedometer

import com.kylecorry.andromeda.sense.v2.ISensorEventReading
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import java.time.Instant

data class StepCounterReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val steps: Int,
    override val accuracy: SensorAccuracy
) : ISensorEventReading

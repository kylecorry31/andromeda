package com.kylecorry.andromeda.sense.v2.pedometer

import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import java.time.Instant

data class StepDetectorReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val stepDetected: Boolean,
    override val accuracy: SensorAccuracy
) : SensorAccuracyReading

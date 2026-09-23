package com.kylecorry.andromeda.sense.v2.clinometer

import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import java.time.Instant

data class ClinometerReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val angle: Float,
    val incline: Float,
    override val accuracy: SensorAccuracy
) : SensorAccuracyReading

package com.kylecorry.andromeda.sense.v2.magnetometer

import com.kylecorry.andromeda.sense.v2.ISensorEventReading
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import java.time.Instant

data class MagnetometerReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val x: Float,
    val y: Float,
    val z: Float,
    override val accuracy: SensorAccuracy
) : ISensorEventReading

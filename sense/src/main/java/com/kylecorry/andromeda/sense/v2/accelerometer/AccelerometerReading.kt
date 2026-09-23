package com.kylecorry.andromeda.sense.v2.accelerometer

import com.kylecorry.andromeda.sense.v2.ISensorEventReading
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import java.time.Instant

data class AccelerometerReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val x: Float,
    val y: Float,
    val z: Float,
    override val accuracy: SensorAccuracy
) : ISensorEventReading

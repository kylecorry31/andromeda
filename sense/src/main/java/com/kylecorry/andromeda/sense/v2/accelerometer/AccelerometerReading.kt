package com.kylecorry.andromeda.sense.v2.accelerometer

import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.sol.math.Vector3
import java.time.Instant

data class AccelerometerReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val acceleration: Vector3,
    override val accuracy: SensorAccuracy
) : SensorAccuracyReading

package com.kylecorry.andromeda.sense.v2.gyroscope

import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.sol.math.Vector3
import java.time.Instant

data class GyroscopeReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val angularRate: Vector3,
    val bias: Vector3?,
    override val accuracy: SensorAccuracy
) : SensorAccuracyReading

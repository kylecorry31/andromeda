package com.kylecorry.andromeda.sense.v2.magnetometer

import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.sol.math.Vector3
import java.time.Instant

data class MagnetometerReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val magneticField: Vector3,
    override val accuracy: SensorAccuracy
) : SensorAccuracyReading

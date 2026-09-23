package com.kylecorry.andromeda.sense.v2.rotation

import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import com.kylecorry.andromeda.sense.v2.readings.QuaternionReading
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.sol.math.Quaternion
import java.time.Instant

data class RotationVectorReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    override val quaternion: Quaternion,
    val headingAccuracy: Float?,
    override val accuracy: SensorAccuracy
) : SensorAccuracyReading, QuaternionReading

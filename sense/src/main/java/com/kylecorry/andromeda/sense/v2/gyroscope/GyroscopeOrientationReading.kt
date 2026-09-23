package com.kylecorry.andromeda.sense.v2.gyroscope

import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import com.kylecorry.andromeda.sense.v2.readings.QuaternionReading
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.sol.math.Quaternion
import java.time.Instant

data class GyroscopeOrientationReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    override val quaternion: Quaternion,
    override val accuracy: SensorAccuracy
) : SensorAccuracyReading, QuaternionReading

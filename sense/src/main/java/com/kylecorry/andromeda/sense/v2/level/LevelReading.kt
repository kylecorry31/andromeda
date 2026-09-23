package com.kylecorry.andromeda.sense.v2.level

import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import java.time.Instant

data class LevelReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val x: Float,
    val y: Float,
    override val accuracy: SensorAccuracy
) : SensorAccuracyReading

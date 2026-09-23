package com.kylecorry.andromeda.sense.v2.temperature

import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.sol.units.Temperature
import java.time.Instant

data class TemperatureReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val temperature: Temperature,
    override val accuracy: SensorAccuracy
) : SensorAccuracyReading

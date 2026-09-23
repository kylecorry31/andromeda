package com.kylecorry.andromeda.sense.v2.barometer

import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.sol.units.Pressure
import java.time.Instant

data class BarometerReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val pressure: Pressure,
    override val accuracy: SensorAccuracy
) : SensorAccuracyReading

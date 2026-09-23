package com.kylecorry.andromeda.sense.v2.altitude

import com.kylecorry.andromeda.sense.v2.readings.AltitudeReading
import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.sol.units.Distance
import java.time.Instant

data class BarometricAltimeterReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    override val altitude: Distance,
    override val accuracy: SensorAccuracy
) : SensorAccuracyReading, AltitudeReading

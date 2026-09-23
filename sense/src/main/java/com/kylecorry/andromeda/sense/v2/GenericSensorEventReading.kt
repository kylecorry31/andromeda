package com.kylecorry.andromeda.sense.v2

import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import java.time.Instant

data class GenericSensorEventReading(
    override val eventTime: Instant,
    override val eventTimeElapsedNanos: Long,
    val values: FloatArray,
    override val accuracy: SensorAccuracy
) : SensorAccuracyReading {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenericSensorEventReading

        if (eventTimeElapsedNanos != other.eventTimeElapsedNanos) return false
        if (eventTime != other.eventTime) return false
        if (!values.contentEquals(other.values)) return false
        if (accuracy != other.accuracy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = eventTimeElapsedNanos.hashCode()
        result = 31 * result + eventTime.hashCode()
        result = 31 * result + values.contentHashCode()
        result = 31 * result + accuracy.hashCode()
        return result
    }
}

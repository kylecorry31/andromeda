package com.kylecorry.andromeda.sense.v2.orientation

import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.andromeda.sense.v2.accelerometer.AccelerometerReading
import com.kylecorry.andromeda.sense.v2.magnetometer.MagnetometerReading
import com.kylecorry.sol.math.Vector3
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds

class DerivedOrientationSensorFlowTest {
    @Test
    fun gravityOrientationMapsAccelerometerReading() = runBlocking {
        val eventTime = Instant.parse("2025-01-01T00:00:00Z")
        val eventTimeElapsedNanos = 123L
        val source = FakeSensorFlow(
            AccelerometerReading(
                eventTime,
                eventTimeElapsedNanos,
                Vector3(0f, 0f, 9.8f),
                SensorAccuracy.High
            )
        )

        val reading = withTimeout(5_000.milliseconds) {
            GravityOrientationSensorFlow(source).flow.first()
        }

        assertEquals(eventTime, reading.eventTime)
        assertEquals(eventTimeElapsedNanos, reading.eventTimeElapsedNanos)
        assertEquals(SensorAccuracy.High, reading.accuracy)
        assertTrue(reading.quaternion.x.isFinite())
        assertTrue(reading.quaternion.y.isFinite())
        assertTrue(reading.quaternion.z.isFinite())
        assertTrue(reading.quaternion.w.isFinite())
    }

    @Test
    fun geomagneticOrientationCombinesInputsAndUsesLatestTimestamp() = runBlocking {
        val magneticTime = Instant.parse("2025-01-01T00:00:00Z")
        val accelerationTime = magneticTime.plusSeconds(1)
        val magnetometer = FakeSensorFlow(
            MagnetometerReading(
                magneticTime,
                100L,
                Vector3(0f, 50f, 0f),
                SensorAccuracy.Medium
            )
        )
        val accelerometer = FakeSensorFlow(
            AccelerometerReading(
                accelerationTime,
                200L,
                Vector3(0f, 0f, 9.8f),
                SensorAccuracy.High
            )
        )

        val reading = withTimeout(5_000.milliseconds) {
            CustomGeomagneticOrientationSensorFlow(magnetometer, accelerometer).flow.first()
        }

        assertEquals(accelerationTime, reading.eventTime)
        assertEquals(200L, reading.eventTimeElapsedNanos)
        assertEquals(SensorAccuracy.Medium, reading.accuracy)
        assertTrue(reading.quaternion.x.isFinite())
        assertTrue(reading.quaternion.y.isFinite())
        assertTrue(reading.quaternion.z.isFinite())
        assertTrue(reading.quaternion.w.isFinite())
    }
}

private class FakeSensorFlow<T>(private val reading: T) : SensorFlow<T>() {
    override suspend fun ProducerScope<T>.setupListener() {
        setIsActive(true)
        trySend(reading)
        awaitClose { setIsActive(false) }
    }
}

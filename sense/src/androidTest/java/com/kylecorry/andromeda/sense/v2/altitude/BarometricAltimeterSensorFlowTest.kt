package com.kylecorry.andromeda.sense.v2.altitude

import android.hardware.SensorManager
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.andromeda.sense.v2.barometer.BarometerReading
import com.kylecorry.sol.units.Pressure
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

class BarometricAltimeterSensorFlowTest {
    @Test
    fun computesAltitudeFromPressure() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().context
        val eventTime = Instant.now()
        val eventTimeElapsedNanos = SystemClock.elapsedRealtimeNanos()
        val pressure = Pressure.hpa(1000f)
        val source = FakeSensorFlow(
            BarometerReading(
                eventTime = eventTime,
                eventTimeElapsedNanos = eventTimeElapsedNanos,
                pressure = pressure,
                accuracy = SensorAccuracy.High
            )
        )
        val sensor = BarometricAltimeterSensorFlow(
            source,
            Pressure.hpa(SensorManager.PRESSURE_STANDARD_ATMOSPHERE)
        )

        val reading = withTimeout(5_000.milliseconds) { sensor.flow.first() }

        assertEquals(eventTime, reading.eventTime)
        assertEquals(eventTimeElapsedNanos, reading.eventTimeElapsedNanos)
        assertTrue(reading.altitude.meters().value.isFinite())
        assertEquals(
            SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure.value),
            reading.altitude.meters().value,
            0.01f
        )
    }
}

internal class FakeSensorFlow<T>(private val reading: T? = null) : SensorFlow<T>() {
    override suspend fun ProducerScope<T>.setupListener() {
        setIsActive(true)
        reading?.let { trySend(it) }
        awaitClose { setIsActive(false) }
    }
}

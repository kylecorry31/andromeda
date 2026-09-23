package com.kylecorry.andromeda.sense.v2.altitude

import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import com.kylecorry.andromeda.preferences.SharedPreferences
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.barometer.BarometerReading
import com.kylecorry.andromeda.sense.v2.location.LocationSensorReading
import com.kylecorry.sol.units.Pressure
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds

class FusedAltimeterSensorFlowTest {
    @Test
    fun emitsBarometricAltitudeWhenGpsCalibrationIsDisabled() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().context
        val preferences = SharedPreferences(context, "fused-altimeter-test-${SystemClock.elapsedRealtime()}")
        val seaLevelPressure = Pressure.hpa(1013.25f)
        preferences.putFloat(LAST_SEA_LEVEL_PRESSURE_KEY, seaLevelPressure.value)
        preferences.putInstant(LAST_SEA_LEVEL_PRESSURE_TIME_KEY, Instant.now())

        val eventTime = Instant.now()
        val eventTimeElapsedNanos = SystemClock.elapsedRealtimeNanos()
        val pressure = Pressure.hpa(1000f)
        val barometer = FakeSensorFlow(
            BarometerReading(
                eventTime = eventTime,
                eventTimeElapsedNanos = eventTimeElapsedNanos,
                pressure = pressure,
                accuracy = SensorAccuracy.High
            )
        )
        val gps = FakeSensorFlow<LocationSensorReading>()
        val sensor = FusedAltimeterSensorFlow(
            gps = gps,
            barometer = barometer,
            cache = preferences,
            useContinuousCalibration = false
        )

        val reading = withTimeout(5_000.milliseconds) { sensor.flow.first() }

        assertEquals(eventTime, reading.eventTime)
        assertEquals(eventTimeElapsedNanos, reading.eventTimeElapsedNanos)
        assertTrue(reading.altitude.meters().value.isFinite())
        assertEquals(
            android.hardware.SensorManager.getAltitude(seaLevelPressure.value, pressure.value),
            reading.altitude.meters().value,
            0.01f
        )
        preferences.close()
    }

    private companion object {
        const val LAST_SEA_LEVEL_PRESSURE_KEY = "andromeda_fused_altimeter_last_sea_level_pressure"
        const val LAST_SEA_LEVEL_PRESSURE_TIME_KEY = "andromeda_fused_altimeter_last_sea_level_pressure_time"
    }
}

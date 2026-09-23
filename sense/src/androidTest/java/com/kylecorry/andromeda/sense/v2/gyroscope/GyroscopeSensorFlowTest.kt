package com.kylecorry.andromeda.sense.v2.gyroscope

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class GyroscopeSensorFlowTest {
    @Test
    fun isActiveWhileCollecting() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().context
        assumeHasSensor(context)
        val sensor = GyroscopeSensorFlow(context)
        val collector = launch { sensor.flow.collect() }
        try {
            withTimeout(5_000.milliseconds) { sensor.isActive.first { it } }
            assertTrue(sensor.isActive.value)
        } finally {
            collector.cancelAndJoin()
        }
        withTimeout(5_000.milliseconds) { sensor.isActive.first { !it } }
        assertFalse(sensor.isActive.value)
    }

    @Test
    fun canCollectGyroscopeReadings() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().context
        assumeHasSensor(context)
        val reading = withTimeout(5_000.milliseconds) { GyroscopeSensorFlow(context).flow.first() }
        assertTrue(reading.angularRate.x.isFinite())
        assertTrue(reading.angularRate.y.isFinite())
        assertTrue(reading.angularRate.z.isFinite())
        assertTrue(reading.eventTimeElapsedNanos > 0)
    }

    private fun assumeHasSensor(context: Context) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        assumeTrue("The emulator does not provide a gyroscope", manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null)
    }
}

package com.kylecorry.andromeda.sense.v2

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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class SensorManagerSensorFlowTest {

    @Test
    fun isActiveWhileCollecting() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().context
        assumeHasSensor(context, Sensor.TYPE_ACCELEROMETER)

        val sensor = SensorManagerSensorFlow(
            context,
            Sensor.TYPE_ACCELEROMETER
        )
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
    fun canCollectSensorReadings() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().context
        assumeHasSensor(context, Sensor.TYPE_ACCELEROMETER)

        val sensor = SensorManagerSensorFlow(
            context,
            Sensor.TYPE_ACCELEROMETER
        )
        val reading = withTimeout(5_000.milliseconds) { sensor.flow.first() }

        assertEquals(3, reading.values.size)
        assertTrue(reading.values.all { it.isFinite() })
        assertTrue(reading.eventTimeElapsedNanos > 0)
    }

    private fun assumeHasSensor(context: Context, sensorType: Int) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        assumeTrue(
            "Sensor type $sensorType is unavailable",
            sensorManager.getDefaultSensor(sensorType) != null
        )
    }
}

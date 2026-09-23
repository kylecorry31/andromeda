package com.kylecorry.andromeda.sense.v2.orientation

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

@Suppress("DEPRECATION")
class OrientationSensorFlowTest {
    @Test
    fun isActiveWhileCollecting() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().context
        assumeHasSensor(context)
        val sensor = OrientationSensorFlow(context)
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
    fun canCollectOrientationReadings() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().context
        assumeHasSensor(context)
        val reading = withTimeout(5_000.milliseconds) { OrientationSensorFlow(context).flow.first() }
        assertTrue(reading.orientation.yaw.isFinite())
        assertTrue(reading.orientation.pitch.isFinite())
        assertTrue(reading.orientation.roll.isFinite())
        assertTrue(reading.eventTimeElapsedNanos > 0)
    }

    private fun assumeHasSensor(context: Context) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        assumeTrue("The emulator does not provide a legacy orientation sensor", manager.getDefaultSensor(Sensor.TYPE_ORIENTATION) != null)
    }
}

package com.kylecorry.andromeda.sense.v2.pedometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class StepDetectorSensorFlowTest {
    @Test
    fun isActiveWhileCollecting() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().context
        assumeHasSensor(context)
        val sensor = StepDetectorSensorFlow(context)
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

    private fun assumeHasSensor(context: Context) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        assumeTrue("The emulator does not provide a step detector", manager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null)
    }
}

package com.kylecorry.andromeda.sense.v2.rotation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.test.platform.app.InstrumentationRegistry
import com.kylecorry.andromeda.sense.v2.SensorFlow
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class RotationVectorSensorFlowTest {
    @Test
    fun availableRotationVectorSensorsAreActiveWhileCollecting() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().context
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        for ((sensorType, flow) in createFlows(context)) {
            if (manager.getDefaultSensor(sensorType) == null) continue

            val collector = launch { flow.flow.collect() }
            try {
                withTimeout(5_000.milliseconds) { flow.isActive.first { it } }
                assertTrue(flow.isActive.value)
            } finally {
                collector.cancelAndJoin()
            }
            withTimeout(5_000.milliseconds) { flow.isActive.first { !it } }
            assertFalse(flow.isActive.value)
        }
    }

    @Test
    fun availableRotationVectorSensorsEmitQuaternionReadings() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().context
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        var testedSensor = false
        for ((sensorType, flow) in createFlows(context)) {
            if (manager.getDefaultSensor(sensorType) == null) continue
            testedSensor = true

            val reading = withTimeout(5_000.milliseconds) { flow.flow.first() }
            assertTrue(reading.quaternion.x.isFinite())
            assertTrue(reading.quaternion.y.isFinite())
            assertTrue(reading.quaternion.z.isFinite())
            assertTrue(reading.quaternion.w.isFinite())
            assertTrue(reading.headingAccuracy == null || reading.headingAccuracy.isFinite())
            assertTrue(reading.eventTimeElapsedNanos > 0)
        }
        assertTrue("No rotation vector sensor is available", testedSensor)
    }

    private fun createFlows(context: Context): List<Pair<Int, SensorFlow<RotationVectorReading>>> {
        return listOf(
            Sensor.TYPE_ROTATION_VECTOR to RotationVectorSensorFlow(context),
            Sensor.TYPE_GAME_ROTATION_VECTOR to GameRotationVectorSensorFlow(context),
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR to GeomagneticRotationVectorSensorFlow(context)
        )
    }
}

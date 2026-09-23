package com.kylecorry.andromeda.sense.v2

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.v2.readings.EventTimeReading
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose

abstract class BaseSensorManagerSensorFlow<T : EventTimeReading>(
    context: Context,
    private val sensorType: Int,
    private val config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig(),
    private val wakeUp: Boolean? = null
) : SensorFlow<T>() {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    override suspend fun ProducerScope<T>.setupListener() {
        onStartListening()

        val sensor = if (wakeUp != null) {
            sensorManager.getDefaultSensor(sensorType, wakeUp)
        } else {
            sensorManager.getDefaultSensor(sensorType)
        }
        if (sensor == null) {
            close()
            return
        }

        val listener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) {
                    return
                }
                trySend(map(event))
            }
        }

        if (!sensorManager.registerListener(
                listener,
                sensor,
                config.sensorDelayUs,
                config.maxReportLatencyUs
            )
        ) {
            close()
            return
        }

        setIsActive(true)

        awaitClose {
            sensorManager.unregisterListener(listener)
            setIsActive(false)
        }
    }

    protected open fun onStartListening() = Unit

    abstract fun map(event: SensorEvent): T
}

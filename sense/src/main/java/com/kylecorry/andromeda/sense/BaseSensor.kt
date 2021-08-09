package com.kylecorry.andromeda.sense

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.sensors.Quality

abstract class BaseSensor(
    context: Context,
    private val sensorType: Int,
    private val sensorDelay: Int
) : AbstractSensor() {

    override val quality: Quality
        get() = _accuracy

    private var _accuracy: Quality = Quality.Unknown

    private val sensorManager = context.getSystemService<SensorManager>()
    private val sensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            _accuracy = when (accuracy) {
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> Quality.Poor
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Quality.Moderate
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> Quality.Good
                else -> Quality.Unknown
            }
        }

        override fun onSensorChanged(event: SensorEvent) {
            handleSensorEvent(event)
            _accuracy = when (event.accuracy) {
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> Quality.Poor
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Quality.Moderate
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> Quality.Good
                else -> Quality.Unknown
            }
            notifyListeners()
        }

    }

    override fun startImpl() {
        sensorManager?.getDefaultSensor(sensorType)?.also { sensor ->
            sensorManager.registerListener(
                sensorListener,
                sensor,
                sensorDelay
            )
        }
    }

    override fun stopImpl() {
        sensorManager?.unregisterListener(sensorListener)
    }

    protected abstract fun handleSensorEvent(event: SensorEvent)
}
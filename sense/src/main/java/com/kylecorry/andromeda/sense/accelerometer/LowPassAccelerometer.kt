package com.kylecorry.andromeda.sense.accelerometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.sol.math.filters.LowPassFilter
import com.kylecorry.sol.math.Vector3
import com.kylecorry.andromeda.sense.BaseSensor

class LowPassAccelerometer(
    context: Context,
    sensorDelay: Int = SensorManager.SENSOR_DELAY_GAME,
    filterSize: Float = 0.05f
) :
    BaseSensor(context, Sensor.TYPE_ACCELEROMETER, sensorDelay),
    IAccelerometer {

    override val hasValidReading: Boolean
        get() = gotReading
    private var gotReading = false

    private val filters = listOf(
        LowPassFilter(filterSize),
        LowPassFilter(filterSize),
        LowPassFilter(filterSize)
    )

    private val lock = Object()

    private var _acceleration = floatArrayOf(0f, 0f, 0f)

    override val acceleration: Vector3
        get() {
            return synchronized(lock) {
                Vector3(_acceleration[0], _acceleration[1], _acceleration[2])
            }
        }

    override val rawAcceleration: FloatArray
        get() {
            return synchronized(lock) {
                _acceleration.clone()
            }
        }

    override fun handleSensorEvent(event: SensorEvent) {
        synchronized(lock) {
            _acceleration[0] = filters[0].filter(event.values[0])
            _acceleration[1] = filters[1].filter(event.values[1])
            _acceleration[2] = filters[2].filter(event.values[2])
        }
        gotReading = true
    }

}
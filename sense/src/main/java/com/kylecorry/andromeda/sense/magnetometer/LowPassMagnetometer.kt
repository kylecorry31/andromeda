package com.kylecorry.andromeda.sense.magnetometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.andromeda.core.math.LowPassFilter
import com.kylecorry.andromeda.core.math.Vector3
import com.kylecorry.andromeda.sense.BaseSensor

class LowPassMagnetometer(context: Context) :
    BaseSensor(context, Sensor.TYPE_MAGNETIC_FIELD, SensorManager.SENSOR_DELAY_FASTEST),
    IMagnetometer {

    override val hasValidReading: Boolean
        get() = gotReading
    private var gotReading = false

    private val filterSize = 0.03f
    private val filters = listOf(
        LowPassFilter(filterSize),
        LowPassFilter(filterSize),
        LowPassFilter(filterSize)
    )

    private val lock = Object()

    private var _magField = floatArrayOf(0f, 0f, 0f)

    override val magneticField: Vector3
        get() {
            return synchronized(lock) {
                Vector3(_magField[0], _magField[1], _magField[2])
            }
        }
    override val rawMagneticField: FloatArray
        get() {
            return synchronized(lock) {
                _magField.clone()
            }
        }

    override fun handleSensorEvent(event: SensorEvent) {
        synchronized(lock) {
            _magField[0] = filters[0].filter(event.values[0])
            _magField[1] = filters[1].filter(event.values[1])
            _magField[2] = filters[2].filter(event.values[2])
        }
        gotReading = true
    }

}
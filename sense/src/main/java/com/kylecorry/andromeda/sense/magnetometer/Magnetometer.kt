package com.kylecorry.andromeda.sense.magnetometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.andromeda.core.math.Vector3
import com.kylecorry.andromeda.sense.BaseSensor

class Magnetometer(context: Context) :
    BaseSensor(context, Sensor.TYPE_MAGNETIC_FIELD, SensorManager.SENSOR_DELAY_FASTEST),
    IMagnetometer {

    override val hasValidReading: Boolean
        get() = gotReading
    private var gotReading = false

    override val rawMagneticField: FloatArray
        get() {
            return synchronized(lock) {
                _magField.clone()
            }
        }

    private val lock = Object()

    private var _magField = floatArrayOf(0f, 0f, 0f)

    override val magneticField: Vector3
        get() {
            return synchronized(lock) {
                Vector3(_magField[0], _magField[1], _magField[2])
            }
        }

    override fun handleSensorEvent(event: SensorEvent) {
        synchronized(lock) {
            _magField[0] = event.values[0]
            _magField[1] = event.values[1]
            _magField[2] = event.values[2]
        }
        gotReading = true
    }

}
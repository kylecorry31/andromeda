package com.kylecorry.andromeda.sense.accelerometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.sol.math.Vector3
import com.kylecorry.andromeda.sense.BaseSensor

class GravitySensor(context: Context, sensorDelay: Int = SensorManager.SENSOR_DELAY_FASTEST) :
    BaseSensor(context, Sensor.TYPE_GRAVITY, sensorDelay), IAccelerometer {

    override val hasValidReading: Boolean
        get() = gotReading
    private var gotReading = false

    private val lock = Object()

    private var _acceleration = floatArrayOf(0f, 0f, 0f)

    override val acceleration: Vector3
        get(){
            return synchronized(lock) {
                Vector3(_acceleration[0], _acceleration[1], _acceleration[2])
            }
        }

    override val rawAcceleration: FloatArray
        get(){
            return synchronized(lock){
                _acceleration.clone()
            }
        }

    override fun handleSensorEvent(event: SensorEvent) {
        synchronized(lock){
            _acceleration[0] = event.values[0]
            _acceleration[1] = event.values[1]
            _acceleration[2] = event.values[2]
        }
        gotReading = true
    }

}
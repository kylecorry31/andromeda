package com.kylecorry.andromeda.sense.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.andromeda.core.math.Quaternion
import com.kylecorry.andromeda.core.math.QuaternionMath
import com.kylecorry.andromeda.sense.BaseSensor

class GameRotationSensor(context: Context) :
    BaseSensor(context, Sensor.TYPE_GAME_ROTATION_VECTOR, SensorManager.SENSOR_DELAY_FASTEST),
    IOrientationSensor {

    private val lock = Object()

    override val hasValidReading: Boolean
        get() = _hasReading

    override val orientation: Quaternion
        get() = Quaternion.from(rawOrientation)

    override val rawOrientation: FloatArray
        get() {
            return synchronized(lock) {
                _quaternion
            }
        }

    private val _quaternion = Quaternion.zero.toFloatArray()

    private var _hasReading = false

    override fun handleSensorEvent(event: SensorEvent) {
        synchronized(lock) {
            SensorManager.getQuaternionFromVector(_quaternion, event.values)
            val w = _quaternion[0]
            val x = _quaternion[1]
            val y = _quaternion[2]
            val z = _quaternion[3]
            _quaternion[0] = x
            _quaternion[1] = y
            _quaternion[2] = z
            _quaternion[3] = w
            QuaternionMath.inverse(_quaternion, _quaternion)
        }
        _hasReading = true
    }
}
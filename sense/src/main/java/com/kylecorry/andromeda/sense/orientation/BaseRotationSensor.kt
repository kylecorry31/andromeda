package com.kylecorry.andromeda.sense.orientation

import android.content.Context
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.BaseSensor
import com.kylecorry.sol.math.Quaternion
import com.kylecorry.sol.math.QuaternionMath
import com.kylecorry.sol.math.SolMath.toDegrees

abstract class BaseRotationSensor(context: Context, type: Int, sensorDelay: Int) :
    BaseSensor(context, type, sensorDelay),
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

    override val headingAccuracy: Float?
        get() = synchronized(lock) {
            _headingAccuracy
        }

    private var _headingAccuracy: Float? = null
    private val _quaternion = Quaternion.zero.toFloatArray()

    private var _hasReading = false

    override fun handleSensorEvent(event: SensorEvent) {
        synchronized(lock) {
            onHandleSensorEvent(event)

            // Calculate the quaternion
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

            _headingAccuracy = if (event.values.size > 4 && event.values[4] > 0f) {
                event.values[4].toDegrees()
            } else {
                null
            }
        }
        _hasReading = true
    }

    protected open fun onHandleSensorEvent(event: SensorEvent) {}
}
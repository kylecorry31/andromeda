package com.kylecorry.andromeda.sense.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.sol.math.Quaternion
import com.kylecorry.sol.math.QuaternionMath
import com.kylecorry.sol.units.Bearing
import com.kylecorry.andromeda.sense.BaseSensor
import com.kylecorry.andromeda.sense.compass.ICompass

class GeomagneticRotationSensor(context: Context, private val useTrueNorth: Boolean) :
    BaseSensor(context, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, SensorManager.SENSOR_DELAY_FASTEST),
    IOrientationSensor, ICompass {

    private val lock = Object()

    override val bearing: Bearing
        get() = Bearing(rawBearing)

    override var declination: Float = 0.0f

    override val hasValidReading: Boolean
        get() = _hasReading

    override val rawBearing: Float
        get(){
            val euler = FloatArray(3)
            QuaternionMath.toEuler(_quaternion, euler)
            val yaw = euler[2]
            return if (useTrueNorth) {
                Bearing.getBearing(Bearing.getBearing(yaw) + declination)
            } else {
                Bearing.getBearing(yaw)
            }
        }

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
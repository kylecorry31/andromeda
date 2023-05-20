package com.kylecorry.andromeda.sense.orientation

import android.content.Context
import android.hardware.SensorManager
import com.kylecorry.sol.math.Quaternion
import com.kylecorry.sol.math.QuaternionMath
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.sensors.Quality
import com.kylecorry.andromeda.sense.Sensors
import com.kylecorry.andromeda.sense.accelerometer.GravitySensor
import com.kylecorry.andromeda.sense.accelerometer.IAccelerometer
import com.kylecorry.andromeda.sense.accelerometer.LowPassAccelerometer
import kotlin.math.atan2
import kotlin.math.sqrt

// Algorithm from https://www.digikey.com/en/articles/using-an-accelerometer-for-inclination-sensing
class GravityOrientationSensor(context: Context, sensorDelay: Int = SensorManager.SENSOR_DELAY_FASTEST) : AbstractSensor(), IOrientationSensor {

    override val hasValidReading: Boolean
        get() = gotReading

    private var gotReading = false

    override val quality: Quality
        get() = _quality

    private val lock = Object()

    override val orientation: Quaternion
        get() = Quaternion.from(rawOrientation)

    override val rawOrientation: FloatArray
        get() = synchronized(lock){
            _quaternion.clone()
        }

    private val _quaternion = Quaternion.zero.toFloatArray()

    private var _quality = Quality.Unknown

    private val accelerometer: IAccelerometer =
        if (Sensors.hasGravity(context)) GravitySensor(context, sensorDelay) else LowPassAccelerometer(context, sensorDelay)

    private fun updateSensor(): Boolean {

        // Gravity
        val gravity = accelerometer.rawAcceleration

        val roll = atan2(gravity[0], sqrt(gravity[1] * gravity[1] + gravity[2] * gravity[2])).toDegrees()
        val pitch = -atan2(gravity[1], sqrt(gravity[0] * gravity[0] + gravity[2] * gravity[2])).toDegrees()

        synchronized(lock) {
            QuaternionMath.fromEuler(floatArrayOf(roll, pitch, 0f), _quaternion)
        }

        _quality = accelerometer.quality

        gotReading = true
        notifyListeners()
        return true
    }

    override fun startImpl() {
        accelerometer.start(this::updateSensor)
    }

    override fun stopImpl() {
        accelerometer.stop(this::updateSensor)
    }

}
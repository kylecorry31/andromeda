package com.kylecorry.andromeda.sense.orientation

import android.hardware.SensorManager
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.sensors.Quality
import com.kylecorry.andromeda.sense.accelerometer.IAccelerometer
import com.kylecorry.sol.math.Quaternion
import com.kylecorry.sol.math.QuaternionMath
import kotlin.math.sign
import kotlin.math.sqrt

/**
 * A sensor which uses the gravity sensor to determine orientation
 * Can't be used for heading, but will maintain a consistent heading of 0 (may have bugs right now)
 */
class GravityRotationSensor(
    private val accelerometer: IAccelerometer
) : AbstractSensor(), IOrientationSensor {

    private val rotationMatrix = FloatArray(16)
    private val _quaternion = Quaternion.zero.toFloatArray()
    private val temp = FloatArray(4)
    private val mockMagneticField = FloatArray(3)

    private val lock = Object()

    override val headingAccuracy: Float?
        get() = null

    override fun startImpl() {
        accelerometer.start(this::onSensorUpdate)
    }

    override fun stopImpl() {
        accelerometer.stop(this::onSensorUpdate)
    }

    private fun onSensorUpdate(): Boolean {
        synchronized(lock) {
            // TODO: There's definitely a better way to do this
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometer.rawAcceleration,
                getMockMagneticField()
            )

            val trace = rotationMatrix[0] + rotationMatrix[5] + rotationMatrix[10]
            val r = sqrt(1 + trace)
            val s = 1 / (2 * r)
            temp[0] = (rotationMatrix[6] - rotationMatrix[9]) * s
            temp[1] = (rotationMatrix[8] - rotationMatrix[2]) * s
            temp[2] = (rotationMatrix[1] - rotationMatrix[4]) * s
            temp[3] = r / 2

            // TODO: Instead of inverting, use the correct values from the rotation matrix
            QuaternionMath.inverse(temp, temp)
            temp.copyInto(_quaternion)
        }

        notifyListeners()

        return true
    }

    private fun getMockMagneticField(): FloatArray {
        val gravity = accelerometer.rawAcceleration
        mockMagneticField[0] = gravity[0] * sign(gravity[2]) * sign(gravity[1])
        mockMagneticField[1] = gravity[1] * sign(gravity[2]) * sign(gravity[1])
        return mockMagneticField
    }


    override val orientation: Quaternion
        get() = Quaternion.from(rawOrientation)

    override val rawOrientation: FloatArray
        get() {
            return synchronized(lock) {
                _quaternion
            }
        }

    override val hasValidReading: Boolean
        get() = accelerometer.hasValidReading

    override val quality: Quality
        get() = accelerometer.quality
}
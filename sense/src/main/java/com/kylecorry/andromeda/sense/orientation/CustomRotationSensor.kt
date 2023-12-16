package com.kylecorry.andromeda.sense.orientation

import android.hardware.SensorManager
import com.kylecorry.andromeda.core.coroutines.onMain
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.sense.accelerometer.IAccelerometer
import com.kylecorry.andromeda.sense.compass.ICompass
import com.kylecorry.andromeda.sense.magnetometer.IMagnetometer
import com.kylecorry.luna.coroutines.CoroutineQueueRunner
import com.kylecorry.sol.math.Quaternion
import com.kylecorry.sol.math.QuaternionMath
import com.kylecorry.sol.units.Bearing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class CustomRotationSensor(
    private val magnetometer: IMagnetometer,
    private val accelerometer: IAccelerometer,
    private val gyro: IGyroscope,
    private val gyroWeight: Float = 0.998f
) : AbstractSensor(), IOrientationSensor {

    private val rotationMatrix = FloatArray(16)
    private val _quaternion = Quaternion.zero.toFloatArray()

    private val lock = Object()

    private val scope = CoroutineScope(Dispatchers.Default)
    private val runner = CoroutineQueueRunner()

    override fun startImpl() {
        isInitialized = false
        magnetometer.start(this::onSensorUpdate)
        accelerometer.start(this::onSensorUpdate)
        gyro.start(this::onGyroUpdate)
    }

    override fun stopImpl() {
        magnetometer.stop(this::onSensorUpdate)
        accelerometer.stop(this::onSensorUpdate)
        gyro.stop(this::onGyroUpdate)
        runner.cancel()
    }

    private var isInitialized = false

    private var lastGyro = Quaternion.zero.toFloatArray()

    private val temp = FloatArray(4)

    private suspend fun update() {
        synchronized(lock) {
            if (!accelerometer.hasValidReading || !magnetometer.hasValidReading) {
                return
            }
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometer.rawAcceleration,
                magnetometer.rawMagneticField
            )

            val trace = rotationMatrix[0] + rotationMatrix[5] + rotationMatrix[10]
            val r = sqrt(1 + trace)
            val s = 1 / (2 * r)
            var w = r / 2
            var x = (rotationMatrix[6] - rotationMatrix[9]) * s
            var y = (rotationMatrix[8] - rotationMatrix[2]) * s
            var z = (rotationMatrix[1] - rotationMatrix[4]) * s

            // TODO: Instead of inverting, use the correct values from the rotation matrix
            temp[0] = x
            temp[1] = y
            temp[2] = z
            temp[3] = w
            QuaternionMath.inverse(temp, temp)
            x = temp[0]
            y = temp[1]
            z = temp[2]
            w = temp[3]

            // Calculate the change from the gyro
            QuaternionMath.subtractRotation(gyro.rawOrientation, lastGyro, temp)
            lastGyro = gyro.rawOrientation.clone()
            QuaternionMath.multiply(_quaternion, temp, temp)
            val gx = temp[0]
            val gy = temp[1]
            val gz = temp[2]
            val gw = temp[3]

            // Complementary filter
            val alpha = if (isInitialized) {
                gyroWeight
            } else {
                isInitialized = true
                0f
            }
            _quaternion[0] = x * (1 - alpha) + gx * alpha
            _quaternion[1] = y * (1 - alpha) + gy * alpha
            _quaternion[2] = z * (1 - alpha) + gz * alpha
            _quaternion[3] = w * (1 - alpha) + gw * alpha
            QuaternionMath.normalize(_quaternion, _quaternion)
        }

        onMain {
            notifyListeners()
        }
    }

    private fun onSensorUpdate(): Boolean {
        return true
    }

    private fun onGyroUpdate(): Boolean {
        scope.launch {
            runner.enqueue {
                update()
            }
        }
        return true
    }

    override val orientation: Quaternion
        get() = Quaternion.from(rawOrientation)

    override val rawOrientation: FloatArray
        get() {
            return synchronized(lock) {
                _quaternion
            }
        }

    override val headingAccuracy: Float?
        get() = null

    override val hasValidReading: Boolean
        get() = magnetometer.hasValidReading && accelerometer.hasValidReading
}
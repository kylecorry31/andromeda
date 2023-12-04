package com.kylecorry.andromeda.sense.orientation

import android.hardware.SensorManager
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.sense.accelerometer.IAccelerometer
import com.kylecorry.andromeda.sense.compass.ICompass
import com.kylecorry.andromeda.sense.magnetometer.IMagnetometer
import com.kylecorry.sol.math.Quaternion
import com.kylecorry.sol.math.QuaternionMath
import com.kylecorry.sol.units.Bearing
import kotlin.math.sqrt

class CustomGeomagneticRotationSensor(
    private val magnetometer: IMagnetometer,
    private val accelerometer: IAccelerometer,
    private val useTrueNorth: Boolean,
) : AbstractSensor(), IOrientationSensor, ICompass {

    private val rotationMatrix = FloatArray(16)
    private val _quaternion = Quaternion.zero.toFloatArray()
    private val _orientation = OrientationCalculator()
    private var _bearing = 0f
    private val temp = FloatArray(4)

    private val lock = Object()

    override val headingAccuracy: Float?
        get() = null

    override fun startImpl() {
        magnetometer.start(this::onSensorUpdate)
        accelerometer.start(this::onSensorUpdate)
    }

    override fun stopImpl() {
        magnetometer.stop(this::onSensorUpdate)
        accelerometer.stop(this::onSensorUpdate)
    }

    private fun onSensorUpdate(): Boolean {
        synchronized(lock) {
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometer.rawAcceleration,
                magnetometer.rawMagneticField
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

            _bearing = _orientation.getAzimuth(rotationMatrix)
        }

        notifyListeners()

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
    override val bearing: Bearing
        get() = Bearing(rawBearing)

    override var declination: Float = 0.0f

    override val rawBearing: Float
        get() {
            return if (useTrueNorth) {
                Bearing.getBearing(Bearing.getBearing(_bearing) + declination)
            } else {
                Bearing.getBearing(_bearing)
            }
        }

    override val hasValidReading: Boolean
        get() = magnetometer.hasValidReading && accelerometer.hasValidReading
}
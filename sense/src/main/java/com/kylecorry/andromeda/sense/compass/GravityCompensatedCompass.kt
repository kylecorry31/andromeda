package com.kylecorry.andromeda.sense.compass

import android.content.Context
import android.hardware.SensorManager
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.sensors.Quality
import com.kylecorry.andromeda.sense.Sensors
import com.kylecorry.andromeda.sense.accelerometer.GravitySensor
import com.kylecorry.andromeda.sense.accelerometer.IAccelerometer
import com.kylecorry.andromeda.sense.accelerometer.LowPassAccelerometer
import com.kylecorry.andromeda.sense.magnetometer.LowPassMagnetometer
import com.kylecorry.sol.math.SolMath.deltaAngle
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.filters.IFilter
import com.kylecorry.sol.math.filters.MovingAverageFilter
import com.kylecorry.sol.science.geology.Geology
import com.kylecorry.sol.units.Bearing
import kotlin.math.min

/**
 * A compass that is independent of device orientation
 * @param context the context
 * @param useTrueNorth true to use true north, false to use magnetic north
 * @param filter the filter to use to smooth the bearing
 * @param useRotationMatrix true to use Android's rotation matrix approach, false to use a custom vector approach
 */
class GravityCompensatedCompass(
    context: Context,
    private val useTrueNorth: Boolean,
    private val filter: IFilter = MovingAverageFilter(1),
    private val useRotationMatrix: Boolean = false
) :
    AbstractSensor(), ICompass {

    override val hasValidReading: Boolean
        get() = gotReading
    private var gotReading = false

    override val quality: Quality
        get() = _quality
    private var _quality = Quality.Unknown

    private val accelerometer: IAccelerometer =
        if (Sensors.hasGravity(context)) GravitySensor(context) else LowPassAccelerometer(context)
    private val magnetometer = LowPassMagnetometer(context)

    override var declination = 0f

    override val bearing: Bearing
        get() {
            return if (useTrueNorth) {
                Bearing(_filteredBearing).withDeclination(declination)
            } else {
                Bearing(_filteredBearing)
            }
        }
    override val rawBearing: Float
        get() {
            return if (useTrueNorth) {
                Bearing.getBearing(Bearing.getBearing(_filteredBearing) + declination)
            } else {
                Bearing.getBearing(_filteredBearing)
            }
        }

    private var _bearing = 0f
    private var _filteredBearing = 0f

    private var gotMag = false
    private var gotAccel = false

    private val rotationMatrix = FloatArray(9)
    private val inclinationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val remappedMatrix = FloatArray(9)

    private fun updateBearing(newBearing: Float) {
        _bearing += deltaAngle(_bearing, newBearing)
        _filteredBearing = filter.filter(_bearing)
    }

    private fun updateSensor(): Boolean {

        if (!gotAccel || !gotMag) {
            return true
        }

        val accelAccuracy = accelerometer.quality
        val magAccuracy = magnetometer.quality
        _quality = Quality.values()[min(accelAccuracy.ordinal, magAccuracy.ordinal)]

        updateBearing(calculateBearing().value)
        gotReading = true
        notifyListeners()
        return true
    }

    private fun calculateBearing(): Bearing {

        if (!useRotationMatrix) {
            return Geology.getAzimuth(accelerometer.acceleration, magnetometer.magneticField)
        }

        SensorManager.getRotationMatrix(
            rotationMatrix,
            inclinationMatrix,
            accelerometer.rawAcceleration,
            magnetometer.rawMagneticField
        )
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_Y,
            SensorManager.AXIS_MINUS_X,
            remappedMatrix
        )
        SensorManager.getOrientation(remappedMatrix, orientationAngles)

        val azimuth = orientationAngles[0].toDegrees() - 90

        return Bearing(azimuth)
    }


    private fun updateAccel(): Boolean {
        gotAccel = true
        return updateSensor()
    }

    private fun updateMag(): Boolean {
        gotMag = true
        return updateSensor()
    }

    override fun startImpl() {
        accelerometer.start(this::updateAccel)
        magnetometer.start(this::updateMag)
    }

    override fun stopImpl() {
        accelerometer.stop(this::updateAccel)
        magnetometer.stop(this::updateMag)
    }

}
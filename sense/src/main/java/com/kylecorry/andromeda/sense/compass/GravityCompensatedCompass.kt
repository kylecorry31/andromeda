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
import com.kylecorry.sol.science.geology.Geology
import com.kylecorry.sol.units.Bearing
import kotlin.math.min

/**
 * A compass that is independent of device orientation
 * @param context the context
 * @param useTrueNorth true to use true north, false to use magnetic north
 */
class GravityCompensatedCompass(
    context: Context,
    private val useTrueNorth: Boolean,
    sensorDelay: Int = SensorManager.SENSOR_DELAY_FASTEST
) :
    AbstractSensor(), ICompass {

    override val hasValidReading: Boolean
        get() = gotReading
    private var gotReading = false

    override val quality: Quality
        get() = _quality
    private var _quality = Quality.Unknown

    private val accelerometer: IAccelerometer =
        if (Sensors.hasGravity(context)) GravitySensor(
            context,
            sensorDelay
        ) else LowPassAccelerometer(context, sensorDelay)
    private val magnetometer = LowPassMagnetometer(context, sensorDelay)

    override var declination = 0f

    override val bearing: Bearing
        get() {
            return if (useTrueNorth) {
                Bearing(_bearing).withDeclination(declination)
            } else {
                Bearing(_bearing)
            }
        }
    override val rawBearing: Float
        get() {
            return if (useTrueNorth) {
                Bearing.getBearing(Bearing.getBearing(_bearing) + declination)
            } else {
                Bearing.getBearing(_bearing)
            }
        }

    private var _bearing = 0f

    private var gotMag = false
    private var gotAccel = false

    private fun updateSensor(): Boolean {

        if (!gotAccel || !gotMag) {
            return true
        }

        val accelAccuracy = accelerometer.quality
        val magAccuracy = magnetometer.quality
        _quality = Quality.values()[min(accelAccuracy.ordinal, magAccuracy.ordinal)]

        _bearing = calculateBearing().value
        gotReading = true
        notifyListeners()
        return true
    }

    private fun calculateBearing(): Bearing {
        return Geology.getAzimuth(accelerometer.acceleration, magnetometer.magneticField)
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
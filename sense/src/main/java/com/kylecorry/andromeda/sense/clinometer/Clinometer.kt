package com.kylecorry.andromeda.sense.clinometer

import android.content.Context
import android.hardware.SensorManager
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.sensors.Quality
import com.kylecorry.andromeda.sense.Sensors
import com.kylecorry.andromeda.sense.accelerometer.GravitySensor
import com.kylecorry.andromeda.sense.accelerometer.IAccelerometer
import com.kylecorry.andromeda.sense.accelerometer.LowPassAccelerometer
import com.kylecorry.sol.math.SolMath.wrap
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.science.geology.Geology

abstract class Clinometer(context: Context, sensorDelay: Int = SensorManager.SENSOR_DELAY_FASTEST) :
    AbstractSensor(), IClinometer {

    override val angle: Float
        get() = _angle

    override val incline: Float
        get() = Geology.getInclination(_angle)

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

    private var _angle = 0f

    private fun updateSensor(): Boolean {

        val gravity = accelerometer.acceleration
        _quality = accelerometer.quality
        _angle = wrap(calculateUnitAngle(gravity), 0f, 360f)

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

    protected abstract fun calculateUnitAngle(gravity: Vector3): Float

}
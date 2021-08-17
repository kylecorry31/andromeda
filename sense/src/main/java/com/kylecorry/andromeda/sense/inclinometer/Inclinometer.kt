package com.kylecorry.andromeda.sense.inclinometer

import android.content.Context
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.sensors.Quality
import com.kylecorry.andromeda.sense.Sensors
import com.kylecorry.andromeda.sense.accelerometer.GravitySensor
import com.kylecorry.andromeda.sense.accelerometer.IAccelerometer
import com.kylecorry.andromeda.sense.accelerometer.LowPassAccelerometer

class Inclinometer(context: Context) : AbstractSensor(), IInclinometer {

    override val angle: Float
        get() = _angle

    override val hasValidReading: Boolean
        get() = gotReading

    private var gotReading = false

    override val quality: Quality
        get() = _quality
    private var _quality = Quality.Unknown

    private val accelerometer: IAccelerometer =
        if (Sensors.hasGravity(context)) GravitySensor(context) else LowPassAccelerometer(context)

    private var _angle = 0f

    private fun updateSensor(): Boolean {

        // Gravity
        val gravity = accelerometer.acceleration
        _quality = accelerometer.quality
        _angle = InclinationCalculator.calculate(gravity)

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
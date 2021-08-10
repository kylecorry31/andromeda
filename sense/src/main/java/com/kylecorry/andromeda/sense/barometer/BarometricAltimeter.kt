package com.kylecorry.andromeda.sense.barometer

import android.hardware.SensorManager
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.sensors.IAltimeter

class BarometricAltimeter(private val barometer: IBarometer, private val seaLevelPressureFn: () -> Float) : AbstractSensor(),
    IAltimeter {

    override val altitude: Float
        get() = SensorManager.getAltitude(seaLevelPressureFn.invoke(), barometer.pressure)

    override val hasValidReading: Boolean
        get() = barometer.hasValidReading

    private fun onBarometerUpdate(): Boolean {
        notifyListeners()
        return true
    }

    override fun startImpl() {
        barometer.start(this::onBarometerUpdate)
    }

    override fun stopImpl() {
        barometer.stop(this::onBarometerUpdate)
    }
}
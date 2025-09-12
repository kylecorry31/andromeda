package com.kylecorry.andromeda.sense.altitude

import android.hardware.SensorManager
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.sensors.IAltimeter
import com.kylecorry.andromeda.sense.barometer.Barometer
import com.kylecorry.andromeda.sense.barometer.IBarometer
import com.kylecorry.sol.units.Pressure

class BarometricAltimeter(
    private val barometer: IBarometer,
    var seaLevelPressure: Pressure = Pressure.hpa(SensorManager.PRESSURE_STANDARD_ATMOSPHERE)
) : AbstractSensor(), IAltimeter {

    override fun startImpl() {
        barometer.start(this::onSensorUpdate)
    }

    override fun stopImpl() {
        barometer.stop(this::onSensorUpdate)
    }

    override val altitude: Float
        get() = SensorManager.getAltitude(seaLevelPressure.hpa().value, barometer.pressure)

    override val hasValidReading: Boolean
        get() = barometer.hasValidReading

    private fun onSensorUpdate(): Boolean {
        notifyListeners()
        return true
    }
}
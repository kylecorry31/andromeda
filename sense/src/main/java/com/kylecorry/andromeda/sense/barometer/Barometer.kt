package com.kylecorry.andromeda.sense.barometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.BaseSensor

class Barometer(
    context: Context, sensorDelay: Int = SensorManager.SENSOR_DELAY_NORMAL
) : BaseSensor(context, Sensor.TYPE_PRESSURE, sensorDelay), IBarometer {

    override val pressure: Float
        get() = _pressure

    override val hasValidReading: Boolean
        get() = gotReading

    private var _pressure = 0f
    private var gotReading = false

    override fun handleSensorEvent(event: SensorEvent) {
        gotReading = true
        _pressure = event.values[0]
    }

}
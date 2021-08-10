package com.kylecorry.andromeda.sense.pedometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.BaseSensor

class Pedometer(context: Context): IPedometer, BaseSensor(context, Sensor.TYPE_STEP_COUNTER, SensorManager.SENSOR_DELAY_NORMAL) {

    override val steps: Int
        get() = _steps
    private var _steps = 0

    private var _hasReading = false

    override val hasValidReading: Boolean
        get() = _hasReading

    override fun handleSensorEvent(event: SensorEvent) {
        _steps = event.values[0].toInt()
        _hasReading = true
    }
}
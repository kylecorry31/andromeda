package com.kylecorry.andromeda.sense.light

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Handler
import com.kylecorry.andromeda.sense.BaseSensor

class LightSensor(
    context: Context,
    sensorDelay: Int = SensorManager.SENSOR_DELAY_NORMAL,
    handler: Handler? = null
) :
    BaseSensor(context, Sensor.TYPE_LIGHT, sensorDelay, handler = handler),
    ILightSensor {

    override val illuminance: Float
        get() = _illuminance

    override val hasValidReading: Boolean
        get() = gotReading
    private var gotReading = false

    private var _illuminance = 0f

    override fun handleSensorEvent(event: SensorEvent) {
        _illuminance = event.values[0]
        gotReading = true
    }
}
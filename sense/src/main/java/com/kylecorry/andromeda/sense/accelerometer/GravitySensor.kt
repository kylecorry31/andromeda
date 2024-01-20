package com.kylecorry.andromeda.sense.accelerometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.BaseSensor
import com.kylecorry.sol.math.Vector3

class GravitySensor(context: Context, sensorDelay: Int = SensorManager.SENSOR_DELAY_GAME) :
    BaseSensor(context, Sensor.TYPE_GRAVITY, sensorDelay), IAccelerometer {

    override val hasValidReading: Boolean
        get() = gotReading
    private var gotReading = false

    override val acceleration: Vector3
        get() = Vector3(rawAcceleration[0], rawAcceleration[1], rawAcceleration[2])

    override val rawAcceleration = FloatArray(3)

    override fun handleSensorEvent(event: SensorEvent) {
        event.values.copyInto(rawAcceleration, endIndex = 2)
        gotReading = true
    }

}
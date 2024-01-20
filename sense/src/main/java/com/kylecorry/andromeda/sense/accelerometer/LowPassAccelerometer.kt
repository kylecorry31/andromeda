package com.kylecorry.andromeda.sense.accelerometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.sol.math.filters.LowPassFilter
import com.kylecorry.sol.math.Vector3
import com.kylecorry.andromeda.sense.BaseSensor

class LowPassAccelerometer(
    context: Context,
    sensorDelay: Int = SensorManager.SENSOR_DELAY_GAME,
    filterSize: Float = 0.05f
) :
    BaseSensor(context, Sensor.TYPE_ACCELEROMETER, sensorDelay),
    IAccelerometer {

    override val hasValidReading: Boolean
        get() = gotReading
    private var gotReading = false

    private val filters = listOf(
        LowPassFilter(filterSize),
        LowPassFilter(filterSize),
        LowPassFilter(filterSize)
    )

    override val acceleration: Vector3
        get() = Vector3(rawAcceleration[0], rawAcceleration[1], rawAcceleration[2])

    override val rawAcceleration = FloatArray(3)

    override fun handleSensorEvent(event: SensorEvent) {
        rawAcceleration[0] = filters[0].filter(event.values[0])
        rawAcceleration[1] = filters[1].filter(event.values[1])
        rawAcceleration[2] = filters[2].filter(event.values[2])
        gotReading = true
    }

}
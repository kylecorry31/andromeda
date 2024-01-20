package com.kylecorry.andromeda.sense.magnetometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.sol.math.filters.LowPassFilter
import com.kylecorry.sol.math.Vector3
import com.kylecorry.andromeda.sense.BaseSensor

class LowPassMagnetometer(
    context: Context,
    sensorDelay: Int = SensorManager.SENSOR_DELAY_GAME,
    filterSize: Float = 0.03f
) :
    BaseSensor(context, Sensor.TYPE_MAGNETIC_FIELD, sensorDelay),
    IMagnetometer {

    override val hasValidReading: Boolean
        get() = gotReading
    private var gotReading = false

    private val filters = listOf(
        LowPassFilter(filterSize),
        LowPassFilter(filterSize),
        LowPassFilter(filterSize)
    )

    override val magneticField: Vector3
        get() = Vector3(rawMagneticField[0], rawMagneticField[1], rawMagneticField[2])

    override val rawMagneticField = FloatArray(3)

    override fun handleSensorEvent(event: SensorEvent) {
        rawMagneticField[0] = filters[0].filter(event.values[0])
        rawMagneticField[1] = filters[1].filter(event.values[1])
        rawMagneticField[2] = filters[2].filter(event.values[2])
        gotReading = true
    }

}
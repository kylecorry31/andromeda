package com.kylecorry.andromeda.sense.v2.magnetometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.math.filters.LowPassFilter

class LowPassMagnetometerSensorFlow(
    context: Context,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig(
        sensorDelayUs = SensorManager.SENSOR_DELAY_GAME
    ),
    private val filterSize: Float = 0.03f,
    private val maintainStateOnRestart: Boolean = false
) : BaseMagnetometerSensorFlow(context, Sensor.TYPE_MAGNETIC_FIELD, config) {
    private var filters = createFilters()

    override fun onStartListening() {
        if (!maintainStateOnRestart) {
            filters = createFilters()
        }
    }

    override fun map(event: SensorEvent): MagnetometerReading {
        val reading = super.map(event)
        return reading.copy(
            magneticField = Vector3(
                filters[0].filter(event.values[0]),
                filters[1].filter(event.values[1]),
                filters[2].filter(event.values[2])
            )
        )
    }

    private fun createFilters() = listOf(
        LowPassFilter(filterSize),
        LowPassFilter(filterSize),
        LowPassFilter(filterSize)
    )
}

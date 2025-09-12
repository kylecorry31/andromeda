package com.kylecorry.andromeda.sense.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.BaseSensor
import com.kylecorry.sol.units.Bearing

@Suppress("DEPRECATION")
class LegacyCompass(
    context: Context,
    private val useTrueNorth: Boolean,
    sensorDelay: Int = SensorManager.SENSOR_DELAY_GAME,
) :
    BaseSensor(context, Sensor.TYPE_ORIENTATION, sensorDelay),
    ICompass {

    override val hasValidReading: Boolean
        get() = gotReading
    private var gotReading = false

    override var declination = 0f

    override val bearing: Bearing
        get() = Bearing.from(rawBearing)

    override val rawBearing: Float
        get() = if (useTrueNorth) {
            Bearing.getBearing(Bearing.getBearing(_bearing) + declination)
        } else {
            Bearing.getBearing(_bearing)
        }

    private var _bearing = 0f

    override fun handleSensorEvent(event: SensorEvent) {
        _bearing = event.values[0]
        gotReading = true
    }
}
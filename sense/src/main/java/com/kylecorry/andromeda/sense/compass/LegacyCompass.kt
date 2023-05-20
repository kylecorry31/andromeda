package com.kylecorry.andromeda.sense.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.sol.units.Bearing
import com.kylecorry.andromeda.sense.BaseSensor
import kotlin.math.abs
import kotlin.math.floor

@Suppress("DEPRECATION")
class LegacyCompass(
    context: Context,
    private val useTrueNorth: Boolean
) :
    BaseSensor(context, Sensor.TYPE_ORIENTATION, SensorManager.SENSOR_DELAY_FASTEST),
    ICompass {

    override val hasValidReading: Boolean
        get() = gotReading
    private var gotReading = false

    override var declination = 0f

    override val bearing: Bearing
        get() {
            return if (useTrueNorth) {
                Bearing(_bearing).withDeclination(declination)
            } else {
                Bearing(_bearing)
            }
        }

    override val rawBearing: Float
        get() {
            return if (useTrueNorth) {
                Bearing.getBearing(Bearing.getBearing(_bearing) + declination)
            } else {
                Bearing.getBearing(_bearing)
            }
        }

    private var _bearing = 0f

    override fun handleSensorEvent(event: SensorEvent) {
        _bearing = event.values[0]
        gotReading = true
    }
}
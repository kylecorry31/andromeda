package com.kylecorry.andromeda.sense.orientation

import android.content.Context
import android.hardware.SensorEvent
import com.kylecorry.andromeda.sense.compass.ICompass
import com.kylecorry.sol.units.Bearing

abstract class BaseWorldRotationSensor(
    context: Context,
    private val useTrueNorth: Boolean,
    type: Int,
    sensorDelay: Int
) :
    BaseRotationSensor(context, type, sensorDelay), ICompass {

    override val bearing: Bearing
        get() = Bearing(rawBearing)

    override var declination: Float = 0.0f

    override val rawBearing: Float
        get() {
            return if (useTrueNorth) {
                Bearing.getBearing(Bearing.getBearing(_bearing) + declination)
            } else {
                Bearing.getBearing(_bearing)
            }
        }

    private val _orientation = OrientationCalculator()
    private var _bearing = 0f

    override fun onHandleSensorEvent(event: SensorEvent) {
        super.onHandleSensorEvent(event)
        _bearing = _orientation.getAzimuth(event.values)
    }
}
package com.kylecorry.andromeda.sense.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.sol.math.filters.MovingAverageFilter
import com.kylecorry.sol.units.Bearing
import com.kylecorry.andromeda.sense.BaseSensor
import com.kylecorry.sol.math.filters.IFilter
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max

@Suppress("DEPRECATION")
class LegacyCompass(
    context: Context,
    private val useTrueNorth: Boolean,
    private val filter: IFilter = MovingAverageFilter(1)
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
                Bearing(_filteredBearing).withDeclination(declination)
            } else {
                Bearing(_filteredBearing)
            }
        }

    override val rawBearing: Float
        get() {
            return if (useTrueNorth) {
                Bearing.getBearing(Bearing.getBearing(_filteredBearing) + declination)
            } else {
                Bearing.getBearing(_filteredBearing)
            }
        }

    private var _bearing = 0f
    private var _filteredBearing = 0f

    override fun handleSensorEvent(event: SensorEvent) {
        _bearing += deltaAngle(_bearing, event.values[0])

        _filteredBearing = filter.filter(_bearing)
        gotReading = true
    }

    private fun deltaAngle(angle1: Float, angle2: Float): Float {
        var delta = angle2 - angle1
        delta += 180
        delta -= floor(delta / 360) * 360
        delta -= 180
        if (abs(abs(delta) - 180) <= Float.MIN_VALUE) {
            delta = 180f
        }
        return delta
    }

}
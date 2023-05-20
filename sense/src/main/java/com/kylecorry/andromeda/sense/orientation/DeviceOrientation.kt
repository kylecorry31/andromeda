package com.kylecorry.andromeda.sense.orientation

import android.content.Context
import android.hardware.SensorManager
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.sense.Sensors
import com.kylecorry.andromeda.sense.accelerometer.GravitySensor
import com.kylecorry.andromeda.sense.accelerometer.IAccelerometer
import com.kylecorry.andromeda.sense.accelerometer.LowPassAccelerometer
import kotlin.math.abs
import kotlin.math.withSign

// TODO: Use the quaternions from the other sensors
class DeviceOrientation(
    private val context: Context,
    sensorDelay: Int = SensorManager.SENSOR_DELAY_FASTEST
) : AbstractSensor() {
    override fun startImpl() {
        accelerometer.start(this::onAccelerometer)
    }

    override fun stopImpl() {
        accelerometer.stop(this::onAccelerometer)
    }

    override val hasValidReading: Boolean
        get() = gotReading

    var orientation: Orientation = Orientation.Flat
        private set

    private val accelerometer: IAccelerometer by lazy {
        if (Sensors.hasGravity(context)) GravitySensor(
            context,
            sensorDelay
        ) else LowPassAccelerometer(context, sensorDelay)
    }

    private var gotReading = false

    private fun onAccelerometer(): Boolean {
        val acceleration = accelerometer.rawAcceleration
        var largestAccelAxis = 0
        for (i in acceleration.indices) {
            if (abs(acceleration[i]) > abs(acceleration[largestAccelAxis])) {
                largestAccelAxis = i
            }
        }

        largestAccelAxis = (largestAccelAxis + 1).toDouble()
            .withSign(acceleration[largestAccelAxis].toDouble()).toInt()

        orientation = when (largestAccelAxis) {
            -3 -> Orientation.FlatInverse
            -2 -> Orientation.PortraitInverse
            -1 -> Orientation.LandscapeInverse
            1 -> Orientation.Landscape
            2 -> Orientation.Portrait
            else -> Orientation.Flat
        }

        gotReading = true

        notifyListeners()
        return true
    }

    enum class Orientation {
        Portrait,
        PortraitInverse,
        Flat,
        FlatInverse,
        Landscape,
        LandscapeInverse
    }

}
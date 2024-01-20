package com.kylecorry.andromeda.sense.mock

import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.accelerometer.IAccelerometer
import com.kylecorry.sol.math.Vector3

class MockAccelerometer(
    private val mockAcceleration: FloatArray = floatArrayOf(
        0f, 0f, SensorManager.STANDARD_GRAVITY
    ), interval: Long = 0
) : MockSensor(interval), IAccelerometer {
    override val acceleration: Vector3
        get() = Vector3.from(rawAcceleration)

    override val rawAcceleration = mockAcceleration
}
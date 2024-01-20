package com.kylecorry.andromeda.sense.mock

import com.kylecorry.andromeda.sense.magnetometer.IMagnetometer
import com.kylecorry.sol.math.Vector3

class MockMagnetometer(
    private val mockMagField: FloatArray = floatArrayOf(
        0f, 1f, 0f
    ), interval: Long = 0
) : MockSensor(interval), IMagnetometer {
    override val magneticField: Vector3
        get() = Vector3.from(rawMagneticField)
    override val rawMagneticField = mockMagField
}
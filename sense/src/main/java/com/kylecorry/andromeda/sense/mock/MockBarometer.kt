package com.kylecorry.andromeda.sense.mock

import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.accelerometer.IAccelerometer
import com.kylecorry.andromeda.sense.barometer.IBarometer
import com.kylecorry.sol.math.Vector3

class MockBarometer(
    private val mockPressure: Float = SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
    interval: Long = 0
) : MockSensor(interval), IBarometer {
    override val pressure: Float
        get() = mockPressure
}
package com.kylecorry.andromeda.sense.mock

import com.kylecorry.andromeda.sense.orientation.IGyroscope
import com.kylecorry.sol.math.Euler
import com.kylecorry.sol.math.Quaternion

class MockGyroscope(interval: Long = 0) : MockSensor(interval), IGyroscope {
    override val angularRate: Euler = Euler(0f, 0f, 0f)
    override val rawAngularRate: FloatArray = floatArrayOf(0f, 0f, 0f)
    override val orientation: Quaternion = Quaternion.zero
    override val rawOrientation: FloatArray = Quaternion.zero.toFloatArray()
    override val headingAccuracy: Float? = null
}
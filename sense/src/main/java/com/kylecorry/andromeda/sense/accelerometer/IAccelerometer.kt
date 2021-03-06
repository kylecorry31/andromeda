package com.kylecorry.andromeda.sense.accelerometer

import com.kylecorry.sol.math.Vector3
import com.kylecorry.andromeda.core.sensors.ISensor

interface IAccelerometer: ISensor {
    val acceleration: Vector3
    val rawAcceleration: FloatArray
}
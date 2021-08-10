package com.kylecorry.andromeda.sense.orientation

import com.kylecorry.andromeda.core.math.Quaternion
import com.kylecorry.andromeda.core.sensors.ISensor

interface IOrientationSensor: ISensor {
    val orientation: Quaternion
    val rawOrientation: FloatArray
}
package com.kylecorry.andromeda.sense.orientation

import com.kylecorry.andromeda.core.math.Euler

interface IGyroscope: IOrientationSensor {
    val angularRate: Euler
    val rawAngularRate: FloatArray
}
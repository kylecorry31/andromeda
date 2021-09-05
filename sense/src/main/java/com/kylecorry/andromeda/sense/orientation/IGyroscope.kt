package com.kylecorry.andromeda.sense.orientation

import com.kylecorry.sol.math.Euler

interface IGyroscope: IOrientationSensor {
    val angularRate: Euler
    val rawAngularRate: FloatArray
}
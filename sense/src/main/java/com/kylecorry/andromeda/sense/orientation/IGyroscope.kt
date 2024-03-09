package com.kylecorry.andromeda.sense.orientation

import com.kylecorry.sol.math.Euler

interface IGyroscope : IOrientationSensor {

    /**
     * The angular rate of the device in degrees per second
     */
    val angularRate: Euler

    /**
     * The raw angular rate of the device in degrees per second
     */
    val rawAngularRate: FloatArray
}
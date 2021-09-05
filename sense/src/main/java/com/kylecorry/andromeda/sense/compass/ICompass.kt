package com.kylecorry.andromeda.sense.compass

import com.kylecorry.andromeda.core.sensors.ISensor
import com.kylecorry.sol.units.Bearing

interface ICompass: ISensor {
    val bearing: Bearing
    val rawBearing: Float
    var declination: Float
}
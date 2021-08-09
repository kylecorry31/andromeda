package com.kylecorry.andromeda.core.sensors

import com.kylecorry.andromeda.core.sensors.ISensor

interface IAltimeter: ISensor {
    val altitude: Float
}
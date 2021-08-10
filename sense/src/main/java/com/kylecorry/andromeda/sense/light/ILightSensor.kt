package com.kylecorry.andromeda.sense.light

import com.kylecorry.andromeda.core.sensors.ISensor

interface ILightSensor: ISensor {
    val illuminance: Float
}
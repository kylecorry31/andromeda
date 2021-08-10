package com.kylecorry.andromeda.sense.pedometer

import com.kylecorry.andromeda.core.sensors.ISensor

interface IPedometer: ISensor {
    val steps: Int
}
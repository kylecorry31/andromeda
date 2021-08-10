package com.kylecorry.andromeda.sense.hygrometer

import com.kylecorry.andromeda.core.sensors.ISensor

interface IHygrometer: ISensor {
    val humidity: Float
}
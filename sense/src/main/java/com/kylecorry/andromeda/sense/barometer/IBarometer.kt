package com.kylecorry.andromeda.sense.barometer

import com.kylecorry.andromeda.core.sensors.ISensor

interface IBarometer: ISensor {
    val pressure: Float
}
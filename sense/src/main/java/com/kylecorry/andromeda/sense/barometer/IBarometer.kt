package com.kylecorry.andromeda.sense.barometer

import com.kylecorry.andromeda.core.sensors.IAltimeter
import com.kylecorry.andromeda.core.sensors.ISensor

interface IBarometer: ISensor, IAltimeter {
    val pressure: Float
}
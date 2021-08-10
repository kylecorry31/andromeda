package com.kylecorry.andromeda.sense.inclinometer

import com.kylecorry.andromeda.core.sensors.ISensor

interface IInclinometer: ISensor {
    val angle: Float
}
package com.kylecorry.andromeda.core.sensors

import com.kylecorry.andromeda.core.units.Speed

interface ISpeedometer: ISensor {
    val speed: Speed
}
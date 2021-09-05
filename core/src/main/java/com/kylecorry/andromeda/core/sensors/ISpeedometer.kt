package com.kylecorry.andromeda.core.sensors

import com.kylecorry.sol.units.Speed

interface ISpeedometer: ISensor {
    val speed: Speed
}
package com.kylecorry.andromeda.core.sensors

import com.kylecorry.andromeda.core.sensors.ISensor
import java.time.Instant

interface IClock: ISensor {
    val time: Instant
}
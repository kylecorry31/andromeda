package com.kylecorry.andromeda.core.sensors

import java.time.Instant

interface IClock: ISensor {
    val time: Instant
}
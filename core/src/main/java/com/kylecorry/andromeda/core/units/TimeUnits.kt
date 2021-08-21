package com.kylecorry.andromeda.core.units

enum class TimeUnits(val id: Int, val seconds: Float) {
    Milliseconds(1, 1 / 1000f),
    Seconds(2, 1f),
    Minutes(3, 60f),
    Hours(4, 3600f),
    Days(5, 86400f)
}
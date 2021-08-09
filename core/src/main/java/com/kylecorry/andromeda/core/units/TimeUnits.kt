package com.kylecorry.andromeda.core.units

enum class TimeUnits(val seconds: Float) {
    Milliseconds(1 / 1000f),
    Seconds(1f),
    Minutes(60f),
    Hours(3600f),
    Days(86400f)
}
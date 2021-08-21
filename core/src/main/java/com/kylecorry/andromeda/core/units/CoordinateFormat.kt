package com.kylecorry.andromeda.core.units

enum class CoordinateFormat(val id: Int) {
    DecimalDegrees(1),
    DegreesDecimalMinutes(2),
    DegreesMinutesSeconds(3),
    UTM(4),
    MGRS(5),
    USNG(6),
    OSNG_OSGB36(7)
}
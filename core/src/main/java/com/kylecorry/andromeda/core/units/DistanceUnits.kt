package com.kylecorry.andromeda.core.units

enum class DistanceUnits(val id: Int, val meters: Float) {
    Centimeters(1, 0.01f),
    Inches(2, 1 / (3.28084f * 12f)),
    Miles(3, 5280f / 3.28084f),
    Yards(4, 0.9144f),
    Feet(5, 1 / 3.28084f),
    Kilometers(6, 1000f),
    Meters(7, 1f),
    NauticalMiles(8, 1852f)
}
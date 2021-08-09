package com.kylecorry.andromeda.core.units

enum class DistanceUnits(val meters: Float) {
    Centimeters(0.01f),
    Inches(1 / (3.28084f * 12f)),
    Miles(5280f / 3.28084f),
    Yards(0.9144f),
    Feet(1 / 3.28084f),
    Kilometers(1000f),
    Meters(1f),
    NauticalMiles(1852f)
}
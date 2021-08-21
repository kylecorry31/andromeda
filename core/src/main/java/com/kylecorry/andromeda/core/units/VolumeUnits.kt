package com.kylecorry.andromeda.core.units

enum class VolumeUnits(val id: Int, val liters: Float) {
    Liters(1, 1f),
    Milliliter(2, 0.001f),
    USCups(3, 0.236588f),
    USPints(4, 0.473176f),
    USQuarts(5, 0.946353f),
    USOunces(6, 0.0295735f),
    USGallons(7, 3.78541f),
    ImperialCups(8, 0.284131f),
    ImperialPints(9, 0.568261f),
    ImperialQuarts(10, 1.13652f),
    ImperialOunces(11, 0.0284131f),
    ImperialGallons(12, 4.54609f)
}
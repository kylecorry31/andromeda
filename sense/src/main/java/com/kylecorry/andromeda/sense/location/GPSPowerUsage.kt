package com.kylecorry.andromeda.sense.location

import androidx.core.location.LocationRequestCompat

enum class GPSPowerUsage {
    High,
    Balanced,
    Low;

    internal fun toLocationRequestQuality(): Int {
        return when (this) {
            High -> LocationRequestCompat.QUALITY_HIGH_ACCURACY
            Balanced -> LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY
            Low -> LocationRequestCompat.QUALITY_LOW_POWER
        }
    }
}

package com.kylecorry.andromeda.sense.location

import com.kylecorry.sol.units.Distance
import java.time.Duration

data class LocationRequestConfig(
    val frequency: Duration = Duration.ofSeconds(20),
    val minimumDistance: Distance = Distance.meters(0f),
    val minimumFrequency: Duration? = null,
    val maximumUpdateDelay: Duration? = null,
    val maximumUpdates: Int? = null,
    val maximumDuration: Duration? = null,
    val powerUsage: GPSPowerUsage = GPSPowerUsage.High
)

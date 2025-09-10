package com.kylecorry.andromeda.signal

import com.kylecorry.andromeda.core.sensors.Quality
import java.time.Instant

data class CellSignal(
    val id: String,
    val strength: Float,
    val dbm: Int,
    val quality: Quality,
    val network: CellNetwork,
    val isRegistered: Boolean,
    val time: Instant,
    val timingDistanceMeters: Float? = null,
    val timingDistanceErrorMeters: Float? = null
)

data class CellNetworkQuality(
    val network: CellNetwork,
    val quality: Quality
)




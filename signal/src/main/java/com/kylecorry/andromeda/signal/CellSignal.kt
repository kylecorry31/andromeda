package com.kylecorry.andromeda.signal

import com.kylecorry.andromeda.core.sensors.Quality

data class CellSignal(
    val id: String,
    val strength: Float,
    val dbm: Int,
    val quality: Quality,
    val network: CellNetwork
)

data class CellNetworkQuality(
    val network: CellNetwork,
    val quality: Quality
)




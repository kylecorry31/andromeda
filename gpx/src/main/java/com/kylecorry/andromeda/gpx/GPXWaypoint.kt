package com.kylecorry.andromeda.gpx

import androidx.annotation.ColorInt
import com.kylecorry.sol.units.Coordinate
import java.time.Instant

data class GPXWaypoint(
    val coordinate: Coordinate,
    val name: String? = null,
    val elevation: Float? = null,
    val comment: String? = null,
    val time: Instant? = null,
    val group: String? = null,
    val symbol: String? = null,
    @ColorInt val color: Int? = null
)

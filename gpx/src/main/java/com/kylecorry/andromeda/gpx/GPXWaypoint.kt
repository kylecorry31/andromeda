package com.kylecorry.andromeda.gpx

import com.kylecorry.sol.units.Coordinate
import java.time.Instant

data class GPXWaypoint(
    val coordinate: Coordinate,
    val name: String?,
    val elevation: Float?,
    val comment: String?,
    val time: Instant?,
    val group: String?
)

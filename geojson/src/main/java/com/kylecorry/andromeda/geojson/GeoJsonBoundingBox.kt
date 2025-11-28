package com.kylecorry.andromeda.geojson

import com.kylecorry.sol.science.geology.CoordinateBounds

data class GeoJsonBoundingBox(
    val west: Double,
    val south: Double,
    val east: Double,
    val north: Double,
    val minZ: Double? = null,
    val maxZ: Double? = null
) {
    val bounds: CoordinateBounds = CoordinateBounds(north, east, south, west)
}
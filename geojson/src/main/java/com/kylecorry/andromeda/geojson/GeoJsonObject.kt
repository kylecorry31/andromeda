package com.kylecorry.andromeda.geojson

interface GeoJsonObject {
    val type: String
    val boundingBox: GeoJsonBoundingBox?
}
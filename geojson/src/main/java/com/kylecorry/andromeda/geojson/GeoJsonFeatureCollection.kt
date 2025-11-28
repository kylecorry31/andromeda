package com.kylecorry.andromeda.geojson

data class GeoJsonFeatureCollection(
    val features: List<GeoJsonFeature>,
    override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonObject {
    override val type = "FeatureCollection"
}

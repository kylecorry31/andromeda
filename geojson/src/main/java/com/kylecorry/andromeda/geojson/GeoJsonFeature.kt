package com.kylecorry.andromeda.geojson

data class GeoJsonFeature(
    val id: Any?,
    val geometry: GeoJsonGeometry?,
    val properties: Map<String, Any?>? = null,
    override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonObject {
    override val type = "Feature"
}

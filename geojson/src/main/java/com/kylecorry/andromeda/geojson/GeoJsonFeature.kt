package com.kylecorry.andromeda.geojson

import com.google.gson.annotations.SerializedName

data class GeoJsonFeature(
    @SerializedName("id") val id: Any?,
    @SerializedName("geometry") val geometry: GeoJsonGeometry?,
    @SerializedName("properties") val properties: Map<String, Any?>? = null,
    @SerializedName("bbox") override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonObject {
    override val type = "Feature"
}

package com.kylecorry.andromeda.geojson

import com.google.gson.annotations.SerializedName

data class GeoJsonFeatureCollection(
    @SerializedName("features") val features: List<GeoJsonFeature>,
    @SerializedName("bbox") override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonObject {
    override val type = "FeatureCollection"
}

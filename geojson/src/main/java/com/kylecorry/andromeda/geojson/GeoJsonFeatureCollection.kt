package com.kylecorry.andromeda.geojson

import com.google.gson.annotations.SerializedName

data class GeoJsonFeatureCollection(
    @SerializedName(FIELD_FEATURES) val features: List<GeoJsonFeature>,
    @SerializedName(FIELD_BBOX) override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonObject {
    override val type = TYPE

    companion object {
        internal const val TYPE = "FeatureCollection"
        internal const val FIELD_FEATURES = "features"
        internal const val FIELD_BBOX = "bbox"
    }
}

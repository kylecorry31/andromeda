package com.kylecorry.andromeda.geojson

import com.google.gson.annotations.SerializedName

data class GeoJsonFeature(
    @SerializedName(FIELD_ID) val id: Any?,
    @SerializedName(FIELD_GEOMETRY) val geometry: GeoJsonGeometry?,
    @SerializedName(FIELD_PROPERTIES) val properties: Map<String, Any?>? = null,
    @SerializedName(FIELD_BBOX) override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonObject {
    override val type = TYPE

    companion object {
        internal const val TYPE = "Feature"
        internal const val FIELD_ID = "id"
        internal const val FIELD_GEOMETRY = "geometry"
        internal const val FIELD_PROPERTIES = "properties"
        internal const val FIELD_BBOX = "bbox"
    }
}

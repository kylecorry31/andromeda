package com.kylecorry.andromeda.geojson

import com.google.gson.annotations.SerializedName
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance

data class GeoJsonPosition(val x: Double, val y: Double, val z: Double? = null) {
    val coordinate
        get() = Coordinate(y, x)

    val altitude
        get() = z?.let { Distance.meters(it.toFloat()) }
}

interface GeoJsonGeometry : GeoJsonObject

data class GeoJsonPoint(
    @SerializedName("coordinates") val point: GeoJsonPosition?,
    @SerializedName("bbox") override val boundingBox: GeoJsonBoundingBox? = null
) :
    GeoJsonGeometry {
    override val type = "Point"
}

data class GeoJsonLineString(
    @SerializedName("coordinates") val line: List<GeoJsonPosition>?,
    @SerializedName("bbox") override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonGeometry {
    override val type = "LineString"
}

data class GeoJsonPolygon(
    @SerializedName("coordinates") val polygon: List<List<GeoJsonPosition>>?,
    @SerializedName("bbox") override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonGeometry {
    override val type = "Polygon"
}

data class GeoJsonMultiPoint(
    @SerializedName("coordinates") val points: List<GeoJsonPosition>?,
    @SerializedName("bbox") override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonGeometry {
    override val type = "MultiPoint"
}

data class GeoJsonMultiLineString(
    @SerializedName("coordinates") val lines: List<List<GeoJsonPosition>>?,
    @SerializedName("bbox") override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonGeometry {
    override val type = "MultiLineString"
}

data class GeoJsonMultiPolygon(
    @SerializedName("coordinates") val polygons: List<List<List<GeoJsonPosition>>>?,
    @SerializedName("bbox") override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonGeometry {
    override val type = "MultiPolygon"
}

data class GeoJsonGeometryCollection(
    @SerializedName("geometries") val geometries: List<GeoJsonGeometry>,
    @SerializedName("bbox") override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonGeometry {
    override val type = "GeometryCollection"

}

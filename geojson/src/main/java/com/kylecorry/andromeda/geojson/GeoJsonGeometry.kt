package com.kylecorry.andromeda.geojson

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
    val point: GeoJsonPosition?,
    override val boundingBox: GeoJsonBoundingBox? = null
) :
    GeoJsonGeometry {
    override val type = "Point"
}

data class GeoJsonLineString(
    val line: List<GeoJsonPosition>?,
    override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonGeometry {
    override val type = "LineString"
}

data class GeoJsonPolygon(
    val polygon: List<List<GeoJsonPosition>>?,
    override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonGeometry {
    override val type = "Polygon"
}

data class GeoJsonMultiPoint(
    val points: List<GeoJsonPosition>?,
    override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonGeometry {
    override val type = "MultiPoint"
}

data class GeoJsonMultiLineString(
    val lines: List<List<GeoJsonPosition>>?,
    override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonGeometry {
    override val type = "MultiLineString"
}

data class GeoJsonMultiPolygon(
    val polygons: List<List<List<GeoJsonPosition>>>?,
    override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonGeometry {
    override val type = "MultiPolygon"
}

data class GeoJsonGeometryCollection(
    val geometries: List<GeoJsonGeometry>,
    override val boundingBox: GeoJsonBoundingBox? = null
) : GeoJsonGeometry {
    override val type = "GeometryCollection"

}

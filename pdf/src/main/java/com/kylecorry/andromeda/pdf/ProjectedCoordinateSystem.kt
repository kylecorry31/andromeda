package com.kylecorry.andromeda.pdf

data class ProjectedCoordinateSystem(
    val geographic: GeographicCoordinateSystem,
    val projection: String
)

data class GeographicCoordinateSystem(val datum: Datum, val primeMeridian: Double)

data class Datum(val name: String, val spheroid: Spheroid)

data class Spheroid(val name: String, val semiMajorAxis: Float, val inverseFlattening: Float)
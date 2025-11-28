package com.kylecorry.andromeda.geojson

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GeoJsonGeometryTest {

    @Test
    fun isHolePolygon() {
        // CCW (Exterior)
        val exterior = listOf(
            GeoJsonPosition(100.0, 0.0),
            GeoJsonPosition(101.0, 0.0),
            GeoJsonPosition(101.0, 1.0),
            GeoJsonPosition(100.0, 1.0),
            GeoJsonPosition(100.0, 0.0)
        )

        // CW (Hole)
        val hole = listOf(
            GeoJsonPosition(100.8, 0.8),
            GeoJsonPosition(100.8, 0.2),
            GeoJsonPosition(100.2, 0.2),
            GeoJsonPosition(100.2, 0.8),
            GeoJsonPosition(100.8, 0.8)
        )

        val polygon = GeoJsonPolygon(listOf(exterior, hole))

        assertFalse(polygon.isHole(0))
        assertTrue(polygon.isHole(1))
    }

    @Test
    fun isHoleMultiPolygon() {
        // CCW (Exterior)
        val exterior = listOf(
            GeoJsonPosition(100.0, 0.0),
            GeoJsonPosition(101.0, 0.0),
            GeoJsonPosition(101.0, 1.0),
            GeoJsonPosition(100.0, 1.0),
            GeoJsonPosition(100.0, 0.0)
        )

        // CW (Hole)
        val hole = listOf(
            GeoJsonPosition(100.8, 0.8),
            GeoJsonPosition(100.8, 0.2),
            GeoJsonPosition(100.2, 0.2),
            GeoJsonPosition(100.2, 0.8),
            GeoJsonPosition(100.8, 0.8)
        )

        val multiPolygon = GeoJsonMultiPolygon(listOf(listOf(exterior, hole)))

        assertFalse(multiPolygon.isHole(0, 0))
        assertTrue(multiPolygon.isHole(0, 1))
    }
}

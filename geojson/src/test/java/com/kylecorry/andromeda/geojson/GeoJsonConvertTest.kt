package com.kylecorry.andromeda.geojson

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class GeoJsonConvertTest {

    @ParameterizedTest
    @MethodSource("provideFromJson")
    fun fromJson(json: String, expected: GeoJsonObject) {
        val parsed = GeoJsonConvert.fromJson(json.byteInputStream())
        assertEquals(expected, parsed)
    }

    @ParameterizedTest
    @MethodSource("provideFromJson")
    fun toJson(json: String, expected: GeoJsonObject) {
        val serialized = GeoJsonConvert.toJson(expected)
        val parsed = GeoJsonConvert.fromJson(serialized.byteInputStream())
        assertEquals(expected, parsed)
    }

    companion object {
        @JvmStatic
        fun provideFromJson(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    """{
        "type": "Feature",
        "id": "test-id",
        "properties": {},
        "geometry": {
             "type": "Point",
             "coordinates": [100.0, 0.0]
         }
     }""",
                    GeoJsonFeature("test-id", GeoJsonPoint(GeoJsonPosition(100.0, 0.0)), emptyMap())
                ),
                Arguments.of(
                    """{
        "type": "Feature",
        "id": 2,
        "properties": {},
        "geometry": {
             "type": "LineString",
             "coordinates": [
                 [100.0, 0.0],
                 [101.0, 1.0]
             ]
         }
     }""",
                    GeoJsonFeature(
                        2L,
                        GeoJsonLineString(
                            listOf(
                                GeoJsonPosition(100.0, 0.0),
                                GeoJsonPosition(101.0, 1.0)
                            )
                        ),
                        emptyMap()
                    )
                ),
                Arguments.of(
                    """{
        "type": "Feature",
        "properties": {},
        "geometry": {
             "type": "Polygon",
             "coordinates": [
                 [
                     [100.0, 0.0],
                     [101.0, 0.0],
                     [101.0, 1.0],
                     [100.0, 1.0],
                     [100.0, 0.0]
                 ]
             ]
         }
     }""",
                    GeoJsonFeature(
                        null,
                        GeoJsonPolygon(
                            listOf(
                                listOf(
                                    GeoJsonPosition(100.0, 0.0),
                                    GeoJsonPosition(101.0, 0.0),
                                    GeoJsonPosition(101.0, 1.0),
                                    GeoJsonPosition(100.0, 1.0),
                                    GeoJsonPosition(100.0, 0.0)
                                )
                            )
                        ),
                        emptyMap()
                    )
                ),
                Arguments.of(
                    """{
        "type": "Feature",
        "properties": {},
        "geometry": {
             "type": "Polygon",
             "coordinates": [
                 [
                     [100.0, 0.0],
                     [101.0, 0.0],
                     [101.0, 1.0],
                     [100.0, 1.0],
                     [100.0, 0.0]
                 ],
                 [
                     [100.8, 0.8],
                     [100.8, 0.2],
                     [100.2, 0.2],
                     [100.2, 0.8],
                     [100.8, 0.8]
                 ]
             ]
         }
     }""",
                    GeoJsonFeature(
                        null, GeoJsonPolygon(
                            listOf(
                                listOf(
                                    GeoJsonPosition(100.0, 0.0),
                                    GeoJsonPosition(101.0, 0.0),
                                    GeoJsonPosition(101.0, 1.0),
                                    GeoJsonPosition(100.0, 1.0),
                                    GeoJsonPosition(100.0, 0.0)
                                ),
                                listOf(
                                    GeoJsonPosition(100.8, 0.8),
                                    GeoJsonPosition(100.8, 0.2),
                                    GeoJsonPosition(100.2, 0.2),
                                    GeoJsonPosition(100.2, 0.8),
                                    GeoJsonPosition(100.8, 0.8)
                                )
                            )
                        ), emptyMap()
                    )
                ),
                Arguments.of(
                    """{
        "type": "Feature",
        "properties": {},
        "geometry": {
             "type": "MultiPoint",
             "coordinates": [
                 [100.0, 0.0],
                 [101.0, 1.0]
             ]
         }
     }""",
                    GeoJsonFeature(
                        null,
                        GeoJsonMultiPoint(
                            listOf(
                                GeoJsonPosition(100.0, 0.0),
                                GeoJsonPosition(101.0, 1.0)
                            )
                        ),
                        emptyMap()
                    )
                ),
                Arguments.of(
                    """{
        "type": "Feature",
        "properties": {},
        "geometry": {
             "type": "MultiLineString",
             "coordinates": [
                 [
                     [100.0, 0.0],
                     [101.0, 1.0]
                 ],
                 [
                     [102.0, 2.0],
                     [103.0, 3.0]
                 ]
             ]
         }
     }""",
                    GeoJsonFeature(
                        null, GeoJsonMultiLineString(
                            listOf(
                                listOf(GeoJsonPosition(100.0, 0.0), GeoJsonPosition(101.0, 1.0)),
                                listOf(GeoJsonPosition(102.0, 2.0), GeoJsonPosition(103.0, 3.0))
                            )
                        ), emptyMap()
                    )
                ),
                Arguments.of(
                    """{
        "type": "Feature",
        "properties": {},
        "geometry": {
             "type": "MultiPolygon",
             "coordinates": [
                 [
                     [
                         [102.0, 2.0],
                         [103.0, 2.0],
                         [103.0, 3.0],
                         [102.0, 3.0],
                         [102.0, 2.0]
                     ]
                 ],
                 [
                     [
                         [100.0, 0.0],
                         [101.0, 0.0],
                         [101.0, 1.0],
                         [100.0, 1.0],
                         [100.0, 0.0]
                     ],
                     [
                         [100.2, 0.2],
                         [100.2, 0.8],
                         [100.8, 0.8],
                         [100.8, 0.2],
                         [100.2, 0.2]
                     ]
                 ]
             ]
         }
     }""",
                    GeoJsonFeature(
                        null, GeoJsonMultiPolygon(
                            listOf(
                                listOf(
                                    listOf(
                                        GeoJsonPosition(102.0, 2.0),
                                        GeoJsonPosition(103.0, 2.0),
                                        GeoJsonPosition(103.0, 3.0),
                                        GeoJsonPosition(102.0, 3.0),
                                        GeoJsonPosition(102.0, 2.0)
                                    )
                                ),
                                listOf(
                                    listOf(
                                        GeoJsonPosition(100.0, 0.0),
                                        GeoJsonPosition(101.0, 0.0),
                                        GeoJsonPosition(101.0, 1.0),
                                        GeoJsonPosition(100.0, 1.0),
                                        GeoJsonPosition(100.0, 0.0)
                                    ),
                                    listOf(
                                        GeoJsonPosition(100.2, 0.2),
                                        GeoJsonPosition(100.2, 0.8),
                                        GeoJsonPosition(100.8, 0.8),
                                        GeoJsonPosition(100.8, 0.2),
                                        GeoJsonPosition(100.2, 0.2)
                                    )
                                )
                            )
                        ), emptyMap()
                    )
                ),
                Arguments.of(
                    """{
        "type": "Feature",
        "properties": {},
        "geometry": {
             "type": "GeometryCollection",
             "geometries": [{
                 "type": "Point",
                 "coordinates": [100.0, 0.0]
             }, {
                 "type": "LineString",
                 "coordinates": [
                     [101.0, 0.0],
                     [102.0, 1.0]
                 ]
             }]
         }
     }""",
                    GeoJsonFeature(
                        null, GeoJsonGeometryCollection(
                            listOf(
                                GeoJsonPoint(GeoJsonPosition(100.0, 0.0)),
                                GeoJsonLineString(
                                    listOf(
                                        GeoJsonPosition(101.0, 0.0),
                                        GeoJsonPosition(102.0, 1.0)
                                    )
                                )
                            )
                        ), emptyMap()
                    )
                ),
                Arguments.of(
                    """{
       "type": "FeatureCollection",
       "features": [{
           "type": "Feature",
           "geometry": {
               "type": "Point",
               "coordinates": [102.0, 0.5]
           },
           "properties": {
               "prop0": "value0"
           }
       }, {
           "type": "Feature",
           "geometry": {
               "type": "LineString",
               "coordinates": [
                   [102.0, 0.0],
                   [103.0, 1.0],
                   [104.0, 0.0],
                   [105.0, 1.0]
               ]
           },
           "properties": {
               "prop0": "value0",
               "prop1": 0.0
           }
       }, {
           "type": "Feature",
           "geometry": {
               "type": "Polygon",
               "coordinates": [
                   [
                       [100.0, 0.0],
                       [101.0, 0.0],
                       [101.0, 1.0],
                       [100.0, 1.0],
                       [100.0, 0.0]
                   ]
               ]
           },
           "properties": {
               "prop0": "value0",
               "prop1": {
                   "this": "that"
               }
           }
       }]
   }""",
                    GeoJsonFeatureCollection(
                        listOf(
                            GeoJsonFeature(
                                null,
                                GeoJsonPoint(GeoJsonPosition(102.0, 0.5)),
                                mapOf("prop0" to "value0")
                            ),
                            GeoJsonFeature(
                                null,
                                GeoJsonLineString(
                                    listOf(
                                        GeoJsonPosition(102.0, 0.0),
                                        GeoJsonPosition(103.0, 1.0),
                                        GeoJsonPosition(104.0, 0.0),
                                        GeoJsonPosition(105.0, 1.0)
                                    )
                                ),
                                mapOf("prop0" to "value0", "prop1" to 0.0)
                            ),
                            GeoJsonFeature(
                                null,
                                GeoJsonPolygon(
                                    listOf(
                                        listOf(
                                            GeoJsonPosition(100.0, 0.0),
                                            GeoJsonPosition(101.0, 0.0),
                                            GeoJsonPosition(101.0, 1.0),
                                            GeoJsonPosition(100.0, 1.0),
                                            GeoJsonPosition(100.0, 0.0)
                                        )
                                    )
                                ),
                                mapOf("prop0" to "value0", "prop1" to mapOf("this" to "that"))
                            )
                        )
                    )
                ),
                Arguments.of(
                    """{
                    "type": "Feature",
                    "properties": {
                        "key1": 1,
                        "key2": 1.2,
                        "key3": true,
                        "key4": "Test",
                        "key5": [1, "2", 3.0, false, [5], { "key1": 1 }],
                        "key6": {
                            "key1": 1,
                            "key2": "2",
                            "key3": 3.0,
                            "key4": false,
                            "key5": [5],
                            "key6": { "key1": 1 }
                        }
                    },
                    "bbox": [1.0, 2.0, 3.0, 4.0, 5.0, 6.0]
                }""", GeoJsonFeature(
                        null, null, mapOf(
                            "key1" to 1L,
                            "key2" to 1.2,
                            "key3" to true,
                            "key4" to "Test",
                            "key5" to listOf(1L, "2", 3.0, false, listOf(5L), mapOf("key1" to 1L)),
                            "key6" to mapOf(
                                "key1" to 1L,
                                "key2" to "2",
                                "key3" to 3.0,
                                "key4" to false,
                                "key5" to listOf(5L),
                                "key6" to mapOf("key1" to 1L)
                            )
                        ),
                        GeoJsonBoundingBox(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
                    )
                ),
                Arguments.of(
                    """{
       "type": "FeatureCollection",
       "bbox": [100.0, 0.0, 105.0, 1.0],
       "features": []
       }""",
                    GeoJsonFeatureCollection(
                        emptyList(),
                        GeoJsonBoundingBox(100.0, 0.0, 105.0, 1.0)
                    )
                ),
                Arguments.of(
                    """{
       "type": "Point",
       "coordinates": [100.0, 0.0],
       "bbox": [100.0, 0.0, 100.0, 0.0]
       }""",
                    GeoJsonPoint(
                        GeoJsonPosition(100.0, 0.0),
                        GeoJsonBoundingBox(100.0, 0.0, 100.0, 0.0)
                    )
                )
            )
        }
    }

}
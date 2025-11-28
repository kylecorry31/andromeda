package com.kylecorry.andromeda.geojson

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import java.io.InputStream

object GeoJsonConvert {

    fun fromJson(stream: InputStream): List<GeoJsonObject> {
        val parsed = JsonParser.parseReader(stream.bufferedReader())
        val objects = mutableListOf<GeoJsonObject>()
        if (parsed.isJsonArray) {
            parsed.asJsonArray.forEach {
                if (it.isJsonObject) {
                    val geo = fromJsonObject(it.asJsonObject)
                    if (geo != null) {
                        objects.add(geo)
                    }
                }
            }
        } else if (parsed.isJsonObject) {
            val geo = fromJsonObject(parsed.asJsonObject)
            if (geo != null) {
                objects.add(geo)
            }
        }
        return objects
    }

    private fun fromJsonObject(obj: JsonObject): GeoJsonObject? {
        val type = getType(obj) ?: return null
        return when (type) {
            "Feature" -> parseFeature(obj)
            "FeatureCollection" -> parseFeatureCollection(obj)
            else -> null
        }
    }

    private fun parseFeature(obj: JsonObject): GeoJsonFeature {
        val id = obj.get("id")
        val geometry = obj.get("geometry")
        val properties = obj.get("properties")

        var actualId: Any? = null
        if (id != null && id.isJsonPrimitive) {
            actualId = toPrimitive(id.asJsonPrimitive)
        }

        var actualGeometry: GeoJsonGeometry? = null
        if (geometry != null && geometry.isJsonObject) {
            actualGeometry = parseGeometry(geometry.asJsonObject)
        }

        var actualProperties: Map<String, Any?>? = null
        if (properties != null && properties.isJsonObject) {
            actualProperties = toMap(properties.asJsonObject)
        }

        return GeoJsonFeature(actualId, actualGeometry, actualProperties, getBoundingBox(obj))
    }

    private fun parseFeatureCollection(obj: JsonObject): GeoJsonFeatureCollection {
        val features = obj.get("features")
        val actualFeatures = if (features == null || !features.isJsonArray) {
            emptyList()
        } else {
            features.asJsonArray.mapNotNull {
                if (it != null && it.isJsonObject && getType(it.asJsonObject) == "Feature") {
                    parseFeature(it.asJsonObject)
                } else {
                    null
                }
            }
        }
        return GeoJsonFeatureCollection(actualFeatures, getBoundingBox(obj))
    }

    private fun parseGeometry(obj: JsonObject): GeoJsonGeometry? {
        val type = getType(obj) ?: return null
        return when (type) {
            "Point" -> parsePoint(obj)
            "LineString" -> parseLineString(obj)
            "Polygon" -> parsePolygon(obj)
            "MultiPoint" -> parseMultiPoint(obj)
            "MultiLineString" -> parseMultiLineString(obj)
            "MultiPolygon" -> parseMultiPolygon(obj)
            "GeometryCollection" -> parseGeometryCollection(obj)
            else -> null
        }
    }

    private fun parsePoint(obj: JsonObject): GeoJsonPoint {
        val coordinates = parsePosition(obj.get("coordinates"))
        return GeoJsonPoint(coordinates, getBoundingBox(obj))
    }

    private fun parseLineString(obj: JsonObject): GeoJsonLineString {
        val coordinates = obj.get("coordinates")
        var actualCoordinates: List<GeoJsonPosition>? = null
        if (coordinates != null && coordinates.isJsonArray) {
            actualCoordinates = coordinates.asJsonArray.asList().mapNotNull { parsePosition(it) }
        }
        return GeoJsonLineString(actualCoordinates, getBoundingBox(obj))
    }

    private fun parsePolygon(obj: JsonObject): GeoJsonPolygon {
        val coordinates = obj.get("coordinates")
        var actualCoordinates: List<List<GeoJsonPosition>>? = null
        if (coordinates != null && coordinates.isJsonArray) {
            actualCoordinates = coordinates.asJsonArray.asList().mapNotNull {
                if (it != null && it.isJsonArray) {
                    it.asJsonArray.asList().mapNotNull { parsePosition(it) }
                } else {
                    null
                }
            }
        }
        return GeoJsonPolygon(actualCoordinates, getBoundingBox(obj))
    }

    private fun parseMultiPoint(obj: JsonObject): GeoJsonMultiPoint {
        val coordinates = obj.get("coordinates")
        var actualCoordinates: List<GeoJsonPosition>? = null
        if (coordinates != null && coordinates.isJsonArray) {
            actualCoordinates = coordinates.asJsonArray.asList().mapNotNull { parsePosition(it) }
        }
        return GeoJsonMultiPoint(actualCoordinates, getBoundingBox(obj))
    }

    private fun parseMultiLineString(obj: JsonObject): GeoJsonMultiLineString {
        val coordinates = obj.get("coordinates")
        var actualCoordinates: List<List<GeoJsonPosition>>? = null
        if (coordinates != null && coordinates.isJsonArray) {
            actualCoordinates = coordinates.asJsonArray.asList().mapNotNull {
                if (it != null && it.isJsonArray) {
                    it.asJsonArray.asList().mapNotNull { parsePosition(it) }
                } else {
                    null
                }
            }
        }
        return GeoJsonMultiLineString(actualCoordinates, getBoundingBox(obj))
    }

    private fun parseMultiPolygon(obj: JsonObject): GeoJsonMultiPolygon {
        val coordinates = obj.get("coordinates")
        var actualCoordinates: List<List<List<GeoJsonPosition>>>? = null
        if (coordinates != null && coordinates.isJsonArray) {
            actualCoordinates = coordinates.asJsonArray.asList().mapNotNull {
                if (it != null && it.isJsonArray) {
                    it.asJsonArray.asList().mapNotNull {
                        if (it != null && it.isJsonArray) {
                            it.asJsonArray.asList().mapNotNull {
                                parsePosition(it)
                            }
                        } else {
                            null
                        }
                    }
                } else {
                    null
                }
            }
        }
        return GeoJsonMultiPolygon(actualCoordinates, getBoundingBox(obj))
    }

    private fun parseGeometryCollection(obj: JsonObject): GeoJsonGeometryCollection? {
        val geometries = obj.get("geometries")
        if (geometries == null || !geometries.isJsonArray) {
            return null
        }
        val actualGeometries = geometries.asJsonArray.mapNotNull {
            if (it != null && it.isJsonObject) {
                parseGeometry(it.asJsonObject)
            } else {
                null
            }
        }
        return GeoJsonGeometryCollection(actualGeometries, getBoundingBox(obj))
    }

    private fun getBoundingBox(obj: JsonObject): GeoJsonBoundingBox? {
        val bbox = obj.get("bbox") ?: return null
        if (!bbox.isJsonArray) {
            return null
        }
        val array = toNumberArray(bbox.asJsonArray)
        if (array.size != 4 && array.size != 6) {
            return null
        }

        val west = array[0].toDouble()
        val south = array[1].toDouble()
        val east = array[2].toDouble()
        val north = array[3].toDouble()
        val down = if (array.size == 6) {
            array[4].toDouble()
        } else {
            null
        }
        val up = if (array.size == 6) {
            array[5].toDouble()
        } else {
            null
        }

        return GeoJsonBoundingBox(west, south, east, north, down, up)
    }

    private fun getType(obj: JsonObject): String? {
        val type = obj.get("type") ?: return null
        if (!type.isJsonPrimitive || !type.asJsonPrimitive.isString) {
            return null
        }
        return type.asJsonPrimitive.asString
    }

    private fun toKotlinType(element: JsonElement?): Any? {
        return when {
            element == null || element.isJsonNull -> null
            element.isJsonObject -> toMap(element.asJsonObject)
            element.isJsonArray -> toList(element.asJsonArray)
            element.isJsonPrimitive -> toPrimitive(element.asJsonPrimitive)
            else -> null
        }
    }

    private fun toMap(obj: JsonObject): Map<String, Any?> {
        val actualMap = mutableMapOf<String, Any?>()
        val map = obj.asMap() ?: return actualMap
        for (property in map.keys) {
            val value = map[property]
            actualMap[property] = toKotlinType(value)
        }
        return actualMap
    }

    private fun toPrimitive(el: JsonPrimitive): Any? {
        return when {
            el.isNumber -> if (el.asString.contains(".")) {
                el.asNumber.toDouble()
            } else {
                el.asNumber.toLong()
            }

            el.isBoolean -> el.asBoolean
            el.isString -> el.asString
            else -> null
        }
    }

    private fun toList(array: JsonArray): List<Any?> {
        return array.asList().map { toKotlinType(it) }
    }

    private fun parsePosition(element: JsonElement?): GeoJsonPosition? {
        if (element == null || !element.isJsonArray) {
            return null
        }
        val numbers = toNumberArray(element.asJsonArray)
        return when (numbers.size) {
            2 -> GeoJsonPosition(numbers[0].toDouble(), numbers[1].toDouble())
            3 -> GeoJsonPosition(
                numbers[0].toDouble(),
                numbers[1].toDouble(),
                numbers[2].toDouble()
            )

            else -> null
        }
    }

    private fun toNumberArray(array: JsonArray): List<Number> {
        return array.asList().mapNotNull {
            if (it.isJsonPrimitive && it.asJsonPrimitive.isNumber) {
                it.asJsonPrimitive.asNumber
            } else {
                null
            }
        }
    }

}
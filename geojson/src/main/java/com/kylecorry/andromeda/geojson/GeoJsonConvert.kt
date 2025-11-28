package com.kylecorry.andromeda.geojson

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonToken
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type

object GeoJsonConvert {

    private val gson = GsonBuilder()
        .registerTypeAdapter(GeoJsonObject::class.java, GeoJsonObjectAdapter())
        .registerTypeAdapter(GeoJsonGeometry::class.java, GeoJsonObjectAdapter())
        .registerTypeAdapter(GeoJsonPosition::class.java, GeoJsonPositionAdapter())
        .registerTypeAdapter(GeoJsonBoundingBox::class.java, GeoJsonBoundingBoxAdapter())
        .registerTypeAdapter(GeoJsonFeature::class.java, GeoJsonFeatureSerializer())
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .create()

    fun fromJson(stream: InputStream): List<GeoJsonObject> {
        val reader = gson.newJsonReader(stream.bufferedReader())
        val objects = mutableListOf<GeoJsonObject>()

        try {
            val token = reader.peek()
            if (token == JsonToken.BEGIN_ARRAY) {
                val listType = object : TypeToken<List<GeoJsonObject>>() {}.type
                val list = gson.fromJson<List<GeoJsonObject>>(reader, listType)
                objects.addAll(list)
            } else {
                val obj = gson.fromJson<GeoJsonObject>(reader, GeoJsonObject::class.java)
                if (obj != null) {
                    objects.add(obj)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return objects
    }

    fun toJson(obj: GeoJsonObject): String {
        return gson.toJson(obj)
    }

    fun toJson(obj: GeoJsonObject, stream: OutputStream) {
        val writer = stream.bufferedWriter()
        gson.toJson(obj, writer)
        writer.flush()
    }

    private class GeoJsonObjectAdapter : JsonDeserializer<GeoJsonObject> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): GeoJsonObject {
            val obj = json.asJsonObject
            val type = obj.get("type")?.asString ?: throw JsonParseException("Missing type")

            return when (type) {
                "Feature" -> context.deserialize<GeoJsonFeature>(json, GeoJsonFeature::class.java)
                "FeatureCollection" -> context.deserialize<GeoJsonFeatureCollection>(
                    json,
                    GeoJsonFeatureCollection::class.java
                )

                "Point" -> context.deserialize<GeoJsonPoint>(json, GeoJsonPoint::class.java)
                "LineString" -> context.deserialize<GeoJsonLineString>(
                    json,
                    GeoJsonLineString::class.java
                )

                "Polygon" -> context.deserialize<GeoJsonPolygon>(json, GeoJsonPolygon::class.java)
                "MultiPoint" -> context.deserialize<GeoJsonMultiPoint>(
                    json,
                    GeoJsonMultiPoint::class.java
                )

                "MultiLineString" -> context.deserialize<GeoJsonMultiLineString>(
                    json,
                    GeoJsonMultiLineString::class.java
                )

                "MultiPolygon" -> context.deserialize<GeoJsonMultiPolygon>(
                    json,
                    GeoJsonMultiPolygon::class.java
                )

                "GeometryCollection" -> context.deserialize<GeoJsonGeometryCollection>(
                    json,
                    GeoJsonGeometryCollection::class.java
                )

                else -> throw JsonParseException("Unknown GeoJSON type: $type")
            }
        }
    }

    private class GeoJsonFeatureSerializer : JsonSerializer<GeoJsonFeature> {
        override fun serialize(
            src: GeoJsonFeature,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            val obj = JsonObject()
            obj.addProperty("type", "Feature")

            if (src.boundingBox != null) {
                obj.add("bbox", context.serialize(src.boundingBox))
            }

            if (src.id != null) {
                obj.add("id", context.serialize(src.id))
            }

            // Always add these fields even if null
            obj.add("geometry", context.serialize(src.geometry))
            obj.add("properties", context.serialize(src.properties))

            return obj
        }
    }

    private class GeoJsonPositionAdapter : JsonDeserializer<GeoJsonPosition>,
        JsonSerializer<GeoJsonPosition> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): GeoJsonPosition {
            val array = json.asJsonArray
            val x = array[0].asDouble
            val y = array[1].asDouble
            val z = if (array.size() > 2) array[2].asDouble else null
            return GeoJsonPosition(x, y, z)
        }

        override fun serialize(
            src: GeoJsonPosition,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            val array = JsonArray()
            array.add(src.x)
            array.add(src.y)
            if (src.z != null) {
                array.add(src.z)
            }
            return array
        }
    }

    private class GeoJsonBoundingBoxAdapter : JsonDeserializer<GeoJsonBoundingBox>,
        JsonSerializer<GeoJsonBoundingBox> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): GeoJsonBoundingBox {
            val array = json.asJsonArray
            val west = array[0].asDouble
            val south = array[1].asDouble
            val east = array[2].asDouble
            val north = array[3].asDouble
            val minZ = if (array.size() > 4) array[4].asDouble else null
            val maxZ = if (array.size() > 5) array[5].asDouble else null
            return GeoJsonBoundingBox(west, south, east, north, minZ, maxZ)
        }

        override fun serialize(
            src: GeoJsonBoundingBox,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            val array = JsonArray()
            array.add(src.west)
            array.add(src.south)
            array.add(src.east)
            array.add(src.north)
            if (src.minZ != null && src.maxZ != null) {
                array.add(src.minZ)
                array.add(src.maxZ)
            }
            return array
        }
    }

}

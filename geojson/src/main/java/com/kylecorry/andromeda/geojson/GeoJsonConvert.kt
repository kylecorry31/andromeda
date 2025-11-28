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

    private const val FIELD_TYPE = "type"

    private val gson = GsonBuilder()
        .registerTypeAdapter(GeoJsonObject::class.java, GeoJsonObjectAdapter())
        .registerTypeAdapter(GeoJsonGeometry::class.java, GeoJsonObjectAdapter())
        .registerTypeAdapter(GeoJsonPosition::class.java, GeoJsonPositionAdapter())
        .registerTypeAdapter(GeoJsonBoundingBox::class.java, GeoJsonBoundingBoxAdapter())
        .registerTypeAdapter(GeoJsonFeature::class.java, GeoJsonFeatureSerializer())
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .create()

    fun fromJson(stream: InputStream): GeoJsonObject? {
        val reader = gson.newJsonReader(stream.bufferedReader())
        try {
           return gson.fromJson(reader, GeoJsonObject::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
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
            val type = obj.get(FIELD_TYPE)?.asString ?: throw JsonParseException("Missing type")

            return when (type) {
                GeoJsonFeature.TYPE -> context.deserialize<GeoJsonFeature>(
                    json,
                    GeoJsonFeature::class.java
                )

                GeoJsonFeatureCollection.TYPE -> context.deserialize<GeoJsonFeatureCollection>(
                    json,
                    GeoJsonFeatureCollection::class.java
                )

                GeoJsonPoint.TYPE -> context.deserialize<GeoJsonPoint>(
                    json,
                    GeoJsonPoint::class.java
                )

                GeoJsonLineString.TYPE -> context.deserialize<GeoJsonLineString>(
                    json,
                    GeoJsonLineString::class.java
                )

                GeoJsonPolygon.TYPE -> context.deserialize<GeoJsonPolygon>(
                    json,
                    GeoJsonPolygon::class.java
                )

                GeoJsonMultiPoint.TYPE -> context.deserialize<GeoJsonMultiPoint>(
                    json,
                    GeoJsonMultiPoint::class.java
                )

                GeoJsonMultiLineString.TYPE -> context.deserialize<GeoJsonMultiLineString>(
                    json,
                    GeoJsonMultiLineString::class.java
                )

                GeoJsonMultiPolygon.TYPE -> context.deserialize<GeoJsonMultiPolygon>(
                    json,
                    GeoJsonMultiPolygon::class.java
                )

                GeoJsonGeometryCollection.TYPE -> context.deserialize<GeoJsonGeometryCollection>(
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
            obj.addProperty(FIELD_TYPE, GeoJsonFeature.TYPE)

            if (src.boundingBox != null) {
                obj.add(GeoJsonFeature.FIELD_BBOX, context.serialize(src.boundingBox))
            }

            if (src.id != null) {
                obj.add(GeoJsonFeature.FIELD_ID, context.serialize(src.id))
            }

            // Always add these fields even if null
            obj.add(GeoJsonFeature.FIELD_GEOMETRY, context.serialize(src.geometry))
            obj.add(GeoJsonFeature.FIELD_PROPERTIES, context.serialize(src.properties))

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
            val east = if (array.size() == 6) array[3].asDouble else array[2].asDouble
            val north = if (array.size() == 6) array[4].asDouble else array[3].asDouble
            val minZ = if (array.size() == 6) array[2].asDouble else null
            val maxZ = if (array.size() == 6) array[5].asDouble else null
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
            if (src.minZ != null){
                array.add(src.minZ)
            }
            array.add(src.east)
            array.add(src.north)
            if (src.maxZ != null) {
                array.add(src.maxZ)
            }
            return array
        }
    }

}

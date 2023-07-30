package com.kylecorry.andromeda.gpx

import com.kylecorry.andromeda.core.io.DeserializationException
import com.kylecorry.andromeda.core.io.ISerializer
import com.kylecorry.andromeda.core.io.SerializationException
import java.io.InputStream
import java.io.OutputStream

class GPXSerializer(private val creator: String) : ISerializer<GPXData> {
    override fun serialize(obj: GPXData, stream: OutputStream) {
        try {
            GPXParser.toGPX(obj, creator, stream)
        } catch (e: Exception) {
            throw SerializationException(e.message ?: "Unknown error", e)
        }
    }

    override fun deserialize(stream: InputStream): GPXData {
        return try {
            GPXParser.parse(stream)
        } catch (e: Exception) {
            throw DeserializationException(e.message ?: "Unknown error", e)
        }
    }
}
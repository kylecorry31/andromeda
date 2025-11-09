package com.kylecorry.andromeda.csv

import com.kylecorry.andromeda.core.io.DeserializationException
import com.kylecorry.andromeda.core.io.ISerializer
import com.kylecorry.andromeda.core.io.SerializationException
import java.io.InputStream
import java.io.OutputStream

class CSVSerializer : ISerializer<List<List<String>>> {
    override fun serialize(obj: List<List<String>>, stream: OutputStream) {
        try {
            CSVConvert.toCSV(stream, obj)
        } catch (e: Exception) {
            throw SerializationException(e.message ?: "Unknown error", e)
        }
    }

    override fun deserialize(stream: InputStream): List<List<String>> {
        return try {
            CSVConvert.parse(stream)
        } catch (e: Exception) {
            throw DeserializationException(e.message ?: "Unknown error", e)
        }
    }
}
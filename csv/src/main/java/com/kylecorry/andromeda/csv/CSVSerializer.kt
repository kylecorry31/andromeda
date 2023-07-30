package com.kylecorry.andromeda.csv

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.kylecorry.andromeda.core.io.DeserializationException
import com.kylecorry.andromeda.core.io.ISerializer
import com.kylecorry.andromeda.core.io.SerializationException
import java.io.InputStream
import java.io.OutputStream

class CSVSerializer : ISerializer<List<List<String>>> {
    override fun serialize(obj: List<List<String>>, stream: OutputStream) {
        try {
            csvWriter().writeAll(obj, stream)
        } catch (e: Exception) {
            throw SerializationException(e.message ?: "Unknown error", e)
        }
    }

    override fun deserialize(stream: InputStream): List<List<String>> {
        return try {
            csvReader().readAll(stream)
        } catch (e: Exception) {
            throw DeserializationException(e.message ?: "Unknown error", e)
        }
    }
}
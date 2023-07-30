package com.kylecorry.andromeda.json

import com.google.gson.Gson
import com.kylecorry.andromeda.core.io.ISerializer
import com.kylecorry.andromeda.core.io.SerializationException
import java.io.InputStream
import java.io.OutputStream

class JsonSerializer<T>(private val classOfT: Class<T>) : ISerializer<T> {
    private val gson = Gson()

    override fun serialize(obj: T, stream: OutputStream) {
        try {
            stream.bufferedWriter().use { writer ->
                gson.toJson(obj, writer)
            }
        } catch (e: Exception) {
            throw SerializationException(e.message ?: "Unknown error", e)
        }
    }

    override fun deserialize(stream: InputStream): T {
        try {
            stream.bufferedReader().use { reader ->
                return gson.fromJson(reader, classOfT)
            }
        } catch (e: Exception) {
            throw SerializationException(e.message ?: "Unknown error", e)
        }
    }
}
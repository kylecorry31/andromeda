package com.kylecorry.andromeda.core.io

import java.io.InputStream
import java.io.OutputStream

class TextSerializer : ISerializer<String> {
    override fun serialize(obj: String, stream: OutputStream) {
        try {
            stream.bufferedWriter().use {
                it.write(obj)
            }
        } catch (e: Exception) {
            throw SerializationException(e.message ?: "Unknown error", e)
        }
    }

    override fun deserialize(stream: InputStream): String {
        try {
            return stream.bufferedReader().use {
                it.readText()
            }
        } catch (e: Exception) {
            throw DeserializationException(e.message ?: "Unknown error", e)
        }
    }
}
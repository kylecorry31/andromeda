package com.kylecorry.andromeda.xml

import com.kylecorry.andromeda.core.io.ISerializer
import com.kylecorry.andromeda.core.io.SerializationException
import java.io.InputStream
import java.io.OutputStream

class XMLSerializer(private val isRoot: Boolean = true) : ISerializer<XMLNode> {
    override fun serialize(obj: XMLNode, stream: OutputStream) {
        try {
            XMLConvert.write(obj, stream, isRoot)
        } catch (e: Exception) {
            throw SerializationException(e.message ?: "Unknown error", e)
        }
    }

    override fun deserialize(stream: InputStream): XMLNode {
        try {
            return XMLConvert.parse(stream)
        } catch (e: Exception) {
            throw SerializationException(e.message ?: "Unknown error", e)
        }
    }
}
package com.kylecorry.andromeda.core.io

import java.io.InputStream
import java.io.OutputStream

interface ISerializer<T> {
    /**
     * Serialize an object to a stream. This will not close the stream.
     * It's recommended to call this within a suspend function.
     * @param obj the object to serialize
     * @param stream the stream to serialize to
     * @throws SerializationException if the object could not be serialized
     */
    fun serialize(obj: T, stream: OutputStream)

    /**
     * Deserialize an object from a stream. This will not close the stream.
     * It's recommended to call this within a suspend function.
     * @param stream the stream to deserialize from
     * @return the deserialized object
     * @throws DeserializationException if the object could not be deserialized
     */
    fun deserialize(stream: InputStream): T
}
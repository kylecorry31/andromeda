package com.kylecorry.andromeda.csv

import com.kylecorry.andromeda.core.io.DeserializationException
import com.kylecorry.andromeda.core.io.SerializationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class CSVSerializerTest {

    @Test
    fun serialize() {
        val data = listOf(
            listOf("Number", "Decimal", "String", "Escape"),
            listOf("1", "3.14", "Test", "Testing, \"123\""),
            listOf("2", "4.14", "Test1", "Testing1, \"123\"")
        )
        val serializer = CSVSerializer()
        val stream = ByteArrayOutputStream()
        serializer.serialize(data, stream)

        val expected =
            "Number,Decimal,String,Escape\r\n1,3.14,Test,\"Testing, \"\"123\"\"\"\r\n2,4.14,Test1,\"Testing1, \"\"123\"\"\"\r\n"

        assertEquals(expected, stream.toString())
    }

    @Test
    fun deserialize() {
        val csv =
            "Number,Decimal,String,Escape\r\n1,3.14,Test,\"Testing, \"\"123\"\"\"\r\n2,4.14,Test1,\"Testing1, \"\"123\"\"\"\r\n"
        val expected = listOf(
            listOf("Number", "Decimal", "String", "Escape"),
            listOf("1", "3.14", "Test", "Testing, \"123\""),
            listOf("2", "4.14", "Test1", "Testing1, \"123\"")
        )

        val serializer = CSVSerializer()
        val stream = ByteArrayInputStream(csv.toByteArray())

        val actual = serializer.deserialize(stream)

        assertEquals(expected, actual)
    }

    @Test
    fun serializeWithException() {
        val data = listOf(
            listOf("Number", "Decimal", "String", "Escape"),
            listOf("1", "3.14", "Test", "Testing, \"123\""),
            listOf("2", "4.14", "Test1", "Testing1, \"123\"")
        )
        val serializer = CSVSerializer()

        // Create a bad output stream that throws an exception
        val stream = object : ByteArrayOutputStream() {
            override fun write(b: ByteArray, off: Int, len: Int) {
                throw Exception("Test")
            }
        }

        assertThrows(SerializationException::class.java) { serializer.serialize(data, stream) }
    }

    @Test
    fun deserializeWithException() {
        val csv =
            "Number,Decimal,String,Escape\r\n1,3.14,Test,\"Testing, \"\"123\"\"\"\r\n2,4.14,Test1,\"Testing1, \"\"123\"\"\"\r\n"

        val serializer = CSVSerializer()

        // Create a bad input stream that throws an exception
        val stream = object : ByteArrayInputStream(csv.toByteArray()) {
            override fun read(b: ByteArray, off: Int, len: Int): Int {
                throw Exception("Test")
            }
        }

        assertThrows(DeserializationException::class.java) { serializer.deserialize(stream) }
    }

}
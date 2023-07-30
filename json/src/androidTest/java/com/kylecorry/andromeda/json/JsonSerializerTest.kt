package com.kylecorry.andromeda.json

import org.junit.Test

import org.junit.Assert.*
import java.io.ByteArrayOutputStream

class JsonSerializerTest {

    @Test
    fun serialize() {
        val obj = TestClass(1, 1.2, "test")
        val stream = ByteArrayOutputStream()
        JsonSerializer(TestClass::class.java).serialize(obj, stream)
        assertEquals("{\"other\":1.2,\"something\":1,\"thing\":\"test\"}", stream.toString())
    }

    @Test
    fun deserialize(){
        val json = "{\"other\":1.2,\"something\":1,\"thing\":\"test\"}"
        val obj =JsonSerializer(TestClass::class.java).deserialize(json.byteInputStream())
        val expected = TestClass(1, 1.2, "test")
        assertEquals(expected, obj)
    }

    data class TestClass(val something: Int, val other: Double, val thing: String)

}
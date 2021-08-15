package com.kylecorry.andromeda.json

import org.junit.Test

import org.junit.Assert.*

class JsonConvertTest {

    @Test
    fun toJson() {
        val obj = TestClass(1, 1.2, "test")
        val json = JsonConvert.toJson(obj)
        assertEquals("{\"other\":1.2,\"something\":1,\"thing\":\"test\"}", json)
    }

    @Test
    fun fromJson(){
        val json = "{\"other\":1.2,\"something\":1,\"thing\":\"test\"}"
        val obj = JsonConvert.fromJson<TestClass>(json)
        val expected = TestClass(1, 1.2, "test")
        assertEquals(expected, obj)
    }

    data class TestClass(val something: Int, val other: Double, val thing: String)

}
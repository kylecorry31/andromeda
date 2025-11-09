package com.kylecorry.andromeda.csv

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class CSVConvertTest {

    @Test
    fun toCSV() {
        val data = listOf(
            listOf("Number", "Decimal", "String", "Escape"),
            listOf(1, 3.14, "Test", "Testing, \"123\""),
            listOf(2, 4.14, "Test1", "Testing1, \"123\""),
            listOf(3, 5.14, "Tes\nt2", "Testing2, \"123\"")
        )

        val actual = CSVConvert.toCSV(data)
        val expected = "Number,Decimal,String,Escape\r\n1,3.14,Test,\"Testing, \"\"123\"\"\"\r\n2,4.14,Test1,\"Testing1, \"\"123\"\"\"\r\n3,5.14,\"Tes\nt2\",\"Testing2, \"\"123\"\"\"\r\n"

        assertEquals(expected, actual)

    }

    @Test
    fun parse() {
        val csv = "Number,Decimal,String,Escape,Notes\r\n" +
            "1,3.14,Test,\"Testing, \"\"123\"\"\",\"Simple note\"\r\n" +
            "\"2\",\"4.14\",\"Test1\",\"Testing1, \"\"123\"\"\",\"\"\"Quoted note\"\"\"\r\n" +
            "3,5.14,\"Tes\nt2\",\"Testing2, \"\"123\"\"\",\"Line one\r\nline two\""

        val expected = listOf(
            listOf("Number", "Decimal", "String", "Escape", "Notes"),
            listOf("1", "3.14", "Test", "Testing, \"123\"", "Simple note"),
            listOf("2", "4.14", "Test1", "Testing1, \"123\"", "\"Quoted note\""),
            listOf("3", "5.14", "Tes\nt2", "Testing2, \"123\"", "Line one\r\nline two")
        )

        val actual = CSVConvert.parse(csv)

        assertEquals(expected, actual)
    }
}
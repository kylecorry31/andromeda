package com.kylecorry.andromeda.pdf

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class CRSWellKnownTextConvertTest {

    @Test
    fun toWKTString() {
        val str = "\"Test\""
        val expected = WKTString("Test")
        val actual = CRSWellKnownTextConvert.toWKT(str)
        assertEquals(expected, actual)
    }

    @Test
    fun toWKTNumberNegative() {
        val str = "-1.22"
        val expected = WKTNumber(-1.22)
        val actual = CRSWellKnownTextConvert.toWKT(str)
        assertEquals(expected, actual)
    }

    @Test
    fun toWKTNumber() {
        val str = "1.22"
        val expected = WKTNumber(1.22)
        val actual = CRSWellKnownTextConvert.toWKT(str)
        assertEquals(expected, actual)
    }

    @Test
    fun toWKT() {
        val str = "S1[\"St1\",1.2,100.0,S2[\"St2\",S3[]]]"
        val expected = WKTSection(
            "S1", listOf(
                WKTString("St1"),
                WKTNumber(1.2),
                WKTNumber(100.0),
                WKTSection(
                    "S2", listOf(
                        WKTString("St2"),
                        WKTSection("S3", emptyList())
                    )
                )
            )
        )

        val actual = CRSWellKnownTextConvert.toWKT(str)
        assertEquals(expected, actual)
    }

    @Test
    fun fromWKT() {
        val expected = "S1[\"St1\",1.2,100.0,S2[\"St2\",S3[]]]"

        val wkt = WKTSection(
            "S1", listOf(
                WKTString("St1"),
                WKTNumber(1.2),
                WKTNumber(100.0),
                WKTSection(
                    "S2", listOf(
                        WKTString("St2"),
                        WKTSection("S3", emptyList())
                    )
                )
            )
        )

        val formatted = CRSWellKnownTextConvert.fromWKT(wkt)

        assertEquals(expected, formatted)
    }
}
package com.kylecorry.andromeda.core.units

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DistanceTest {

    @Test
    fun convertTo() {
        val distance = Distance(1f, DistanceUnits.Centimeters)
        val expected = Distance(0.00001f, DistanceUnits.Kilometers)
        assertEquals(expected, distance.convertTo(DistanceUnits.Kilometers))
    }
}
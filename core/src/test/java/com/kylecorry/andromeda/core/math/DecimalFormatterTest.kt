package com.kylecorry.andromeda.core.math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class DecimalFormatterTest {

    @ParameterizedTest
    @MethodSource("provideValues")
    fun format(value: Number, places: Int, strict: Boolean, expected: String) {
        assertEquals(expected, DecimalFormatter.format(value, places, strict))
    }

    companion object {
        @JvmStatic
        fun provideValues(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1.155, 1, false, "1.2"),
                Arguments.of(1.155, 1, true, "1.2"),
                Arguments.of(1.155, 4, false, "1.155"),
                Arguments.of(1.155, 4, true, "1.1550"),
                Arguments.of(1.155, 0, true, "1"),
                Arguments.of(0.155, 2, true, "0.16"),
                Arguments.of(100.155, 2, true, "100.16"),
                Arguments.of(100, 2, true, "100.00"),
                Arguments.of(100, 2, false, "100"),
                Arguments.of(Double.NaN, 2, true, "-"),
                Arguments.of(Double.NEGATIVE_INFINITY, 2, true, "-"),
                Arguments.of(Double.POSITIVE_INFINITY, 2, true, "-"),
                )
        }
    }

}
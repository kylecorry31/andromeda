package com.kylecorry.andromeda.core.math

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class MathExtensionsTest {

    @ParameterizedTest
    @MethodSource("provideDoubleStrings")
    fun toDoubleCompat(str: String, expected: Double?) {
        assertEquals(str.toDoubleCompat(), expected)
    }

    @ParameterizedTest
    @MethodSource("provideIntStrings")
    fun toIntCompat(str: String, expected: Int?) {
        assertEquals(str.toIntCompat(), expected)
    }

    @ParameterizedTest
    @MethodSource("provideFloatStrings")
    fun toFloatCompat(str: String, expected: Float?) {
        assertEquals(str.toFloatCompat(), expected)
    }

    @ParameterizedTest
    @MethodSource("provideLongStrings")
    fun toDoubleCompat(str: String, expected: Long?) {
        assertEquals(str.toLongCompat(), expected)
    }

    companion object {
        @JvmStatic
        fun provideDoubleStrings(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("1.2", 1.2),
                Arguments.of("1,2", 1.2),
                Arguments.of("-1.2", -1.2),
                Arguments.of("test", null),
            )
        }

        @JvmStatic
        fun provideFloatStrings(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("1.2", 1.2f),
                Arguments.of("1,2", 1.2f),
                Arguments.of("-1.2", -1.2f),
                Arguments.of("test", null),
            )
        }

        @JvmStatic
        fun provideIntStrings(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("1", 1),
                Arguments.of("-1", -1),
                Arguments.of("test", null),
            )
        }

        @JvmStatic
        fun provideLongStrings(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("1", 1L),
                Arguments.of("-1", -1L),
                Arguments.of("test", null),
            )
        }
    }
}
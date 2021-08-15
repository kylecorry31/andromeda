package com.kylecorry.andromeda.sense.inclinometer

import com.kylecorry.andromeda.core.math.Vector3

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class InclinationCalculatorTest {

    @ParameterizedTest
    @MethodSource("provideIncline")
    fun calculatesIncline(gravity: Vector3, expected: Float) {
        val angle = InclinationCalculator.calculate(gravity)
        assertEquals(expected, angle, 0.01f)
    }

    companion object {
        @JvmStatic
        fun provideIncline(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Vector3(-9.81f, 0f, 0f), 0f),
                Arguments.of(Vector3(9.81f, 0f, 0f), 0f),
                Arguments.of(Vector3(0f, -9.81f, 0f), -90f),
                Arguments.of(Vector3(0f, 9.81f, 0f), 90f),
                Arguments.of(Vector3(2f, 2f, 0f), 45f),
                Arguments.of(Vector3(-2f, 2f, 0f), 45f),
                Arguments.of(Vector3(2f, -2f, 0f), -45f),
                Arguments.of(Vector3(-2f, -2f, 0f), -45f),
            )
        }
    }
}
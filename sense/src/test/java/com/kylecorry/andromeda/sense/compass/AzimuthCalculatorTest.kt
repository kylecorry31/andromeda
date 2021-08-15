package com.kylecorry.andromeda.sense.compass

import com.kylecorry.andromeda.core.math.Vector3
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class AzimuthCalculatorTest {

    @ParameterizedTest
    @MethodSource("provideVectors")
    fun calculate(gravity: Vector3, magnetic: Vector3, expected: Float?) {
        val bearing = AzimuthCalculator.calculate(gravity, magnetic)?.value

        if (bearing == null || expected == null) {
            assertEquals(bearing, expected)
            return
        }

        assertEquals(expected, bearing, 0.5f)

    }

    companion object {
        @JvmStatic
        fun provideVectors(): Stream<Arguments> {
            return Stream.of(
                // Held flat
                Arguments.of(Vector3(0f, 0f, 9.81f), Vector3(0f, 16f, -51f), 0f),
                Arguments.of(Vector3(0f, 0f, 9.81f), Vector3(16f, 0f, -51f), 270f),
                Arguments.of(Vector3(0f, 0f, 9.81f), Vector3(-16f, 0f, -51f), 90f),
                Arguments.of(Vector3(0f, 0f, 9.81f), Vector3(0f, -16f, -51f), 180f),
                Arguments.of(Vector3(0f, 0f, 9.81f), Vector3(8f, -8f, -51f), 225f),
                Arguments.of(Vector3(0f, 0f, 9.81f), Vector3(-8f, -8f, -51f), 135f),
                Arguments.of(Vector3(0f, 0f, 9.81f), Vector3(-8f, 8f, -51f), 45f),
                Arguments.of(Vector3(0f, 0f, 9.81f), Vector3(8f, 8f, -51f), 315f),
                // Held vertical
                Arguments.of(Vector3(0f, 9.81f, 0f), Vector3(0f, -51f, 16f), 180f),
                Arguments.of(Vector3(0f, 9.81f, 0f), Vector3(16f, -51f, 0f), 270f),
                Arguments.of(Vector3(0f, 9.81f, 0f), Vector3(-16f, -51f, 0f), 90f),
                Arguments.of(Vector3(0f, 9.81f, 0f), Vector3(0f, -51f, -16f), 0f),
                Arguments.of(Vector3(0f, 9.81f, 0f), Vector3(8f, -51f, -8f), 315f),
                Arguments.of(Vector3(0f, 9.81f, 0f), Vector3(-8f, -51f, -8f), 45f),
                Arguments.of(Vector3(0f, 9.81f, 0f), Vector3(-8f, -51f, 8f), 135f),
                Arguments.of(Vector3(0f, 9.81f, 0f), Vector3(8f, -51f, 8f), 225f),
                // In between
                Arguments.of(Vector3(4.1f, 6.8f, 5.6f), Vector3(-35f, -23f, -27f), 58f),
                Arguments.of(Vector3(4.6f, -2.2f, 8.5f), Vector3(-31f, 6f, -34f), 107f),
                // Invalid
                Arguments.of(Vector3(0f, 0f, 0f), Vector3(0f, -51f, 16f), null),
                Arguments.of(Vector3(0f, 0f, 10f), Vector3(0f, 0f, 0f), null),
                Arguments.of(Vector3(10f, 10f, 10f), Vector3(10f, 10f, 10f), null),
            )
        }
    }
}
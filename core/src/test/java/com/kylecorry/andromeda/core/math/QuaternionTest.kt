package com.kylecorry.andromeda.core.math

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class QuaternionTest {

    @Test
    fun toFloatArray() {
        val q = Quaternion(1f, 2f, 3f, 4f)
        assertArrayEquals(floatArrayOf(1f, 2f, 3f, 4f), q.toFloatArray())
    }

    @ParameterizedTest
    @MethodSource("provideTimes")
    fun times(a: Quaternion, b: Quaternion, expected: Quaternion) {
        approxEquals(expected, a * b, 0.001f)
    }

    @ParameterizedTest
    @MethodSource("providePlus")
    fun plus(a: Quaternion, b: Quaternion, expected: Quaternion) {
        approxEquals(expected, a + b, 0.001f)
    }

    @ParameterizedTest
    @MethodSource("provideMinus")
    fun minus(a: Quaternion, b: Quaternion, expected: Quaternion) {
        approxEquals(expected, a - b, 0.001f)
    }

    @ParameterizedTest
    @MethodSource("provideNormal")
    fun normalize(a: Quaternion, expected: Quaternion) {
        approxEquals(expected, a.normalize(), 0.001f)
    }

    @ParameterizedTest
    @MethodSource("provideConjugate")
    fun conjugate(a: Quaternion, expected: Quaternion) {
        approxEquals(expected, a.conjugate(), 0.001f)
    }

    @ParameterizedTest
    @MethodSource("provideInverse")
    fun inverse(a: Quaternion, expected: Quaternion) {
        approxEquals(expected, a.inverse(), 0.001f)
    }

    @ParameterizedTest
    @MethodSource("provideRotate")
    fun rotate(a: Quaternion, v: Vector3, expected: Vector3) {
        approxEquals(expected, a.rotate(v), 0.001f)
    }

    @ParameterizedTest
    @MethodSource("provideEuler")
    fun toEuler(a: Quaternion, expected: Euler) {
        approxEquals(expected, a.toEuler(), 0.001f)
    }

    @ParameterizedTest
    @MethodSource("provideEuler")
    fun fromEuler(expected: Quaternion, a: Euler) {
        approxEquals(expected, Quaternion.from(a), 0.001f)
        approxEquals(expected, a.toQuaternion(), 0.001f)
    }

    @ParameterizedTest
    @MethodSource("provideMagnitude")
    fun magnitude(a: Quaternion, expected: Float) {
        assertEquals(expected, a.magnitude(), 0.001f)
    }

    companion object {

        fun approxEquals(a: Quaternion, b: Quaternion, threshold: Float){
            assertEquals(a.x, b.x, threshold)
            assertEquals(a.y, b.y, threshold)
            assertEquals(a.z, b.z, threshold)
            assertEquals(a.w, b.w, threshold)
        }

        fun approxEquals(a: Vector3, b: Vector3, threshold: Float){
            assertEquals(a.x, b.x, threshold)
            assertEquals(a.y, b.y, threshold)
            assertEquals(a.z, b.z, threshold)
        }

        fun approxEquals(a: Euler, b: Euler, threshold: Float){
            assertEquals(a.roll, b.roll, threshold)
            assertEquals(a.pitch, b.pitch, threshold)
            assertEquals(a.yaw, b.yaw, threshold)
        }

        @JvmStatic
        fun provideTimes(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Quaternion(2f, 3f, 4f, 1f),
                    Quaternion(0.25f, -0.5f, 1f, 0.5f),
                    Quaternion(6.25f, 0f, 1.25f, -2.5f)
                ),
                Arguments.of(
                    Quaternion(0.25f, -0.5f, 1f, 0.5f),
                    Quaternion(2f, 3f, 4f, 1f),
                    Quaternion(-3.75f, 2f, 4.75f, -2.5f)
                )
            )
        }

        @JvmStatic
        fun providePlus(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Quaternion(2f, 3f, 4f, 1f),
                    Quaternion(0.25f, -0.5f, 1f, 0.5f),
                    Quaternion(2.25f, 2.5f, 5f, 1.5f)
                ),
                Arguments.of(
                    Quaternion(0.25f, -0.5f, 1f, 0.5f),
                    Quaternion(2f, 3f, 4f, 1f),
                    Quaternion(2.25f, 2.5f, 5f, 1.5f)
                )
            )
        }

        @JvmStatic
        fun provideMinus(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Quaternion(2f, 3f, 4f, 1f),
                    Quaternion(0.25f, -0.5f, 1f, 0.5f),
                    Quaternion(1.75f, 3.5f, 3f, 0.5f)
                ),
                Arguments.of(
                    Quaternion(0.25f, -0.5f, 1f, 0.5f),
                    Quaternion(2f, 3f, 4f, 1f),
                    Quaternion(-1.75f, -3.5f, -3f, -0.5f)
                )
            )
        }

        @JvmStatic
        fun provideRotate(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Quaternion(0.0f,  0.707f, 0.0f, 0.707f),
                    Vector3(1f, 0f, 0f),
                    Vector3(0f, 0f, -1f)
                )
            )
        }

        @JvmStatic
        fun provideMagnitude(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Quaternion(2f, 3f, 4f, 1f),
                    5.477225575051661f
                ),
                Arguments.of(
                    Quaternion(0.25f, -0.5f, 1f, 0.5f),
                    1.25f
                )
            )
        }

        @JvmStatic
        fun provideNormal(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Quaternion(2f, 3f, 4f, 1f),
                    Quaternion(
                        0.365148371670111f,
                        0.5477225f,
                        0.730296743340222f,
                        0.182574185835055f
                    )
                ),
                Arguments.of(
                    Quaternion(0.25f, -0.5f, 1f, 0.5f),
                    Quaternion(0.2f, -0.4f, 0.8f, 0.4f)
                )
            )
        }

        @JvmStatic
        fun provideConjugate(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Quaternion(2f, 3f, 4f, 1f),
                    Quaternion(-2f, -3f, -4f, 1f)
                ),
                Arguments.of(
                    Quaternion(0.25f, -0.5f, 1f, 0.5f),
                    Quaternion(-0.25f, 0.5f, -1f, 0.5f)
                )
            )
        }

        @JvmStatic
        fun provideInverse(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Quaternion(2f, 3f, 4f, 1f),
                    Quaternion(-0.0666667f,-0.1f,-0.1333333f, 0.0333333f)
                ),
                Arguments.of(
                    Quaternion(0.25f, -0.5f, 1f, 0.5f),
                    Quaternion(-0.16f,0.32f,-0.64f, 0.32f)
                )
            )
        }

        @JvmStatic
        fun provideEuler(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Quaternion(2f, 3f, 4f, 1f).normalize(),
                    Euler(81.87f,-19.471f, 135f)
                ),
            )
        }

    }


}
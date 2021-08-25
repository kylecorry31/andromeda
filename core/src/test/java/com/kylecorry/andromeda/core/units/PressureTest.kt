package com.kylecorry.andromeda.core.units

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class PressureTest {

    @ParameterizedTest
    @MethodSource("providePressures")
    fun convertPressure(pressure: Float, from: PressureUnits, to: PressureUnits, expected: Float) {
        val p = Pressure(pressure, from).convertTo(to)
        assertEquals(expected, p.pressure, 0.01f)
        assertEquals(to, p.units)
    }

    companion object {
        @JvmStatic
        fun providePressures(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1000f, PressureUnits.Hpa, PressureUnits.Inhg, 29.53f),
                Arguments.of(1000f, PressureUnits.Hpa, PressureUnits.Psi, 14.50377f),
                Arguments.of(1000f, PressureUnits.Hpa, PressureUnits.Mbar, 1000f),
                Arguments.of(1000f, PressureUnits.Hpa, PressureUnits.Hpa, 1000f),
                Arguments.of(1000f, PressureUnits.Hpa, PressureUnits.MmHg, 750.06158f),

                Arguments.of(1000f, PressureUnits.Mbar, PressureUnits.Hpa, 1000f),
                Arguments.of(1000f, PressureUnits.Mbar, PressureUnits.Inhg, 29.53f),
                Arguments.of(1000f, PressureUnits.Mbar, PressureUnits.Psi, 14.5037f),
                Arguments.of(1000f, PressureUnits.Mbar, PressureUnits.Mbar, 1000f),
                Arguments.of(1000f, PressureUnits.Mbar, PressureUnits.MmHg, 750.06158f),

                Arguments.of(29.53f, PressureUnits.Inhg, PressureUnits.Hpa, 1000f),
                Arguments.of(29.53f, PressureUnits.Inhg, PressureUnits.Psi, 14.5037f),
                Arguments.of(29.53f, PressureUnits.Inhg, PressureUnits.Mbar, 1000f),
                Arguments.of(29.53f, PressureUnits.Inhg, PressureUnits.Inhg, 29.53f),
                Arguments.of(29.53f, PressureUnits.Inhg, PressureUnits.MmHg, 750.06158f),

                Arguments.of(14.50377f, PressureUnits.Psi, PressureUnits.Hpa, 1000f),
                Arguments.of(14.5037f, PressureUnits.Psi, PressureUnits.Inhg, 29.53f),
                Arguments.of(14.50377f, PressureUnits.Psi, PressureUnits.Mbar, 1000f),
                Arguments.of(14.5037f, PressureUnits.Psi, PressureUnits.Psi, 14.5037f),
                Arguments.of(14.5037f, PressureUnits.Psi, PressureUnits.MmHg, 750.06158f),

                Arguments.of(750.06158f, PressureUnits.MmHg, PressureUnits.Hpa, 1000f),
                Arguments.of(750.06158f, PressureUnits.MmHg, PressureUnits.Inhg, 29.53f),
                Arguments.of(750.06158f, PressureUnits.MmHg, PressureUnits.Mbar, 1000f),
                Arguments.of(750.06158f, PressureUnits.MmHg, PressureUnits.Psi, 14.5037f),
                Arguments.of(750.06158f, PressureUnits.MmHg, PressureUnits.MmHg, 750.06158f)
            )
        }
    }

}
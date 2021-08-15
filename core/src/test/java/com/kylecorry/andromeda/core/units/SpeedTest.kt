package com.kylecorry.andromeda.core.units

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest

import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class SpeedTest {

    @ParameterizedTest
    @MethodSource("provideSpeedConversions")
    fun convertTo(speed: Speed, expected: Speed) {
        assertEquals(expected, speed.convertTo(expected.distanceUnits, expected.timeUnits))
    }

    companion object {

        @JvmStatic
        fun provideSpeedConversions(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Speed(1f, DistanceUnits.Meters, TimeUnits.Seconds), Speed(1f, DistanceUnits.Meters, TimeUnits.Seconds)),
                Arguments.of(Speed(1f, DistanceUnits.Meters, TimeUnits.Seconds), Speed(0.001f, DistanceUnits.Meters, TimeUnits.Milliseconds)),
                Arguments.of(Speed(1f, DistanceUnits.Meters, TimeUnits.Seconds), Speed(60f, DistanceUnits.Meters, TimeUnits.Minutes)),
                Arguments.of(Speed(1f, DistanceUnits.Meters, TimeUnits.Seconds), Speed(3600f, DistanceUnits.Meters, TimeUnits.Hours)),
                Arguments.of(Speed(1f, DistanceUnits.Meters, TimeUnits.Seconds), Speed(86400f, DistanceUnits.Meters, TimeUnits.Days)),
                Arguments.of(Speed(5f, DistanceUnits.Feet, TimeUnits.Hours), Speed(3657.6f, DistanceUnits.Centimeters, TimeUnits.Days)),
                Arguments.of(Speed(86400f, DistanceUnits.Centimeters, TimeUnits.Days), Speed(0.01f, DistanceUnits.Meters, TimeUnits.Seconds))
            )
        }

    }

}
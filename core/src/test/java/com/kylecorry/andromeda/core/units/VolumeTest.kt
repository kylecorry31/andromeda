package com.kylecorry.andromeda.core.units

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class VolumeTest {
    @ParameterizedTest
    @MethodSource("provideConversions")
    fun convertTo(volume: Volume, expected: Volume) {
        val converted = volume.convertTo(expected.units)
        assertEquals(expected.units, converted.units)
        assertEquals(expected.volume, converted.volume, 0.0001f)
    }

    companion object {

        @JvmStatic
        fun provideConversions(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Volume(2f, VolumeUnits.Liters),
                    Volume(0.528344f, VolumeUnits.USGallons)
                ),
                Arguments.of(
                    Volume(3f, VolumeUnits.USGallons),
                    Volume(11.3562f, VolumeUnits.Liters)
                ),
                Arguments.of(
                    Volume(3f, VolumeUnits.ImperialGallons),
                    Volume(461.16525f, VolumeUnits.USOunces)
                ),
                Arguments.of(
                    Volume(4f, VolumeUnits.Milliliter),
                    Volume(0.014078f, VolumeUnits.ImperialCups)
                ),
                Arguments.of(Volume(4f, VolumeUnits.USPints), Volume(2f, VolumeUnits.USQuarts)),
                Arguments.of(
                    Volume(4f, VolumeUnits.USCups),
                    Volume(3.3307f, VolumeUnits.ImperialCups)
                ),
                Arguments.of(
                    Volume(4f, VolumeUnits.ImperialOunces),
                    Volume(0.2f, VolumeUnits.ImperialPints)
                ),
                Arguments.of(
                    Volume(1f, VolumeUnits.ImperialQuarts),
                    Volume(1136.52f, VolumeUnits.Milliliter)
                ),
            )
        }

    }
}
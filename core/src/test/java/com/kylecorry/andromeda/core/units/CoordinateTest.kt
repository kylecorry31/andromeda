package com.kylecorry.andromeda.core.units

import com.kylecorry.andromeda.core.units.CoordinateExtensions.parse
import com.kylecorry.andromeda.core.units.CoordinateExtensions.toDecimalDegrees
import com.kylecorry.andromeda.core.units.CoordinateExtensions.toDegreeDecimalMinutes
import com.kylecorry.andromeda.core.units.CoordinateExtensions.toDegreeMinutesSeconds
import com.kylecorry.andromeda.core.units.CoordinateExtensions.toMGRS
import com.kylecorry.andromeda.core.units.CoordinateExtensions.toOSNG
import com.kylecorry.andromeda.core.units.CoordinateExtensions.toUSNG
import com.kylecorry.andromeda.core.units.CoordinateExtensions.toUTM
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import org.junit.Assert
import org.junit.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class CoordinateTest {

    @Test
    fun toUTM() {
        Assert.assertEquals("19T 0282888E 4674752N", Coordinate(42.1948, -71.6295).toUTM())
        Assert.assertEquals("14T 0328056E 5290773N", Coordinate(47.7474, -101.2939).toUTM())
        Assert.assertEquals("13R 0393008E 3051634N", Coordinate(27.5844, -106.0840).toUTM())
        Assert.assertEquals("21L 0359923E 9098523N", Coordinate(-8.1534, -58.2715).toUTM())
        Assert.assertEquals("34H 0674432E 6430470N", Coordinate(-32.2489, 22.8516).toUTM())
        Assert.assertEquals("34H 0674432E 6430470N", Coordinate(-32.2489, 22.8516).toUTM())
        Assert.assertEquals("34D 0528288E 2071725N", Coordinate(-71.4545, 21.7969).toUTM())
        Assert.assertEquals("40X 0545559E 9051365N", Coordinate(81.5113, 59.7656).toUTM())
        Assert.assertEquals("17M 0784692E 9999203N", Coordinate(-0.0072, -78.4424).toUTM())
        Assert.assertEquals("09E 0353004E 3573063N", Coordinate(-57.9598, -131.4844).toUTM())

        // Different precisions
        Assert.assertEquals("19T 0282888E 4674752N", Coordinate(42.1948, -71.6295).toUTM(7))
        Assert.assertEquals("19T 0282880E 4674750N", Coordinate(42.1948, -71.6295).toUTM(6))
        Assert.assertEquals("19T 0282800E 4674700N", Coordinate(42.1948, -71.6295).toUTM(5))
        Assert.assertEquals("19T 0282000E 4674000N", Coordinate(42.1948, -71.6295).toUTM(4))
        Assert.assertEquals("19T 0280000E 4670000N", Coordinate(42.1948, -71.6295).toUTM(3))
        Assert.assertEquals("19T 0200000E 4600000N", Coordinate(42.1948, -71.6295).toUTM(2))
        Assert.assertEquals("19T 0000000E 4000000N", Coordinate(42.1948, -71.6295).toUTM(1))

        // Polar zones
        Assert.assertEquals("A 1998062E 1888990N", Coordinate(-89.0, -179.0).toUTM())
        Assert.assertEquals("A 1998062E 2111009N", Coordinate(-89.0, -1.0).toUTM())
        Assert.assertEquals("B 2019279E 2109339N", Coordinate(-89.0, 10.0).toUTM())
        Assert.assertEquals("B 2000000E 2111026N", Coordinate(-89.0, 0.0).toUTM())
        Assert.assertEquals("B 2000000E 2000000N", Coordinate(-90.0, 0.0).toUTM())
        Assert.assertEquals("B 2000000E 2000000N", Coordinate(-90.0, -80.0).toUTM())

        Assert.assertEquals("Y 1998062E 2111009N", Coordinate(89.0, -179.0).toUTM())
        Assert.assertEquals("Y 1998062E 1888990N", Coordinate(89.0, -1.0).toUTM())
        Assert.assertEquals("Z 2019279E 1890660N", Coordinate(89.0, 10.0).toUTM())
        Assert.assertEquals("Z 2000000E 1888973N", Coordinate(89.0, 0.0).toUTM())
        Assert.assertEquals("Z 2000000E 2000000N", Coordinate(90.0, 0.0).toUTM())
        Assert.assertEquals("Z 2000000E 2000000N", Coordinate(90.0, -80.0).toUTM())
    }

    @Test
    fun canAddDistance() {
        val start = Coordinate(40.0, 10.0)
        val bearing = Bearing(100f)
        val distance = 10000.0

        val expected = Coordinate(39.984444, 10.115556)
        val actual = start.plus(distance, bearing)
        Assert.assertEquals(expected.latitude, actual.latitude, 0.01)
        Assert.assertEquals(expected.longitude, actual.longitude, 0.01)
    }

    @ParameterizedTest
    @MethodSource("provideDDM")
    fun toDDM(expected: String, coordinate: Coordinate, precision: Int) {
        Assert.assertEquals(expected, coordinate.toDegreeDecimalMinutes(precision))
    }

    @ParameterizedTest
    @MethodSource("provideDMS")
    fun toDMS(expected: String, coordinate: Coordinate, precision: Int) {
        Assert.assertEquals(expected, coordinate.toDegreeMinutesSeconds(precision))
    }

    @ParameterizedTest
    @MethodSource("provideDD")
    fun toDD(expected: String, coordinate: Coordinate, precision: Int) {
        Assert.assertEquals(expected, coordinate.toDecimalDegrees(precision))
    }

    @ParameterizedTest
    @MethodSource("provideMGRS")
    fun toMGRS(expected: String, coordinate: Coordinate, precision: Int) {
        Assert.assertEquals(expected, coordinate.toMGRS(precision))
    }

    @ParameterizedTest
    @MethodSource("provideUSNG")
    fun toUSNG(expected: String, coordinate: Coordinate, precision: Int) {
        Assert.assertEquals(expected, coordinate.toUSNG(precision))
    }

    @ParameterizedTest
    @MethodSource("provideOSNG")
    fun toOSNG(expected: String, coordinate: Coordinate, precision: Int) {
        Assert.assertEquals(expected, coordinate.toOSNG(precision))
    }

    @ParameterizedTest
    @MethodSource("provideLocationStrings")
    fun parse(locationString: String, expected: Coordinate?) {
        assertCoordinatesEqual(Coordinate.parse(locationString), expected, 0.001)
    }

    private fun assertCoordinatesEqual(
        actual: Coordinate?,
        expected: Coordinate?,
        precision: Double
    ) {
        if (expected == null) {
            Assert.assertNull(actual)
            return
        }
        Assert.assertNotNull(actual)
        Assert.assertEquals(expected.latitude, actual!!.latitude, precision)
        Assert.assertEquals(expected.longitude, actual.longitude, precision)
    }

    companion object {

        @JvmStatic
        fun provideDDM(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("10°2.1'N    77°30.5'E", Coordinate(10.03472, 77.508333), 1),
                Arguments.of("10°2.1'S    77°30.5'E", Coordinate(-10.03472, 77.508333), 1),
                Arguments.of("10°2.08'N    77°30.5'W", Coordinate(10.03472, -77.508333), 2),
                Arguments.of("10°2.0832'S    77°30.5'W", Coordinate(-10.03472, -77.508333), 4),
            )
        }

        @JvmStatic
        fun provideDMS(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("10°2'5.0\"N    77°30'30.0\"E", Coordinate(10.03472, 77.508333), 1),
                Arguments.of("10°2'5.0\"S    77°30'30.0\"E", Coordinate(-10.03472, 77.508333), 1),
                Arguments.of("10°2'4.99\"N    77°30'30.0\"W", Coordinate(10.03472, -77.508333), 2),
                Arguments.of("10°7'23.16\"S    77°7'23.232\"W", Coordinate(-10.1231, -77.12312), 3),
            )
        }

        @JvmStatic
        fun provideDD(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("42.1948°,  -71.6295°", Coordinate(42.1948, -71.6295), 4),
                Arguments.of("-90°,  -180°", Coordinate(-90.0, -180.0), 0),
                Arguments.of("-42.19°,  -71.63°", Coordinate(-42.1948, -71.6295), 2),
                Arguments.of("1.2°,  1.4°", Coordinate(1.2, 1.4), 1),
            )
        }

        @JvmStatic
        fun provideMGRS(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("27PXM 09601 05579", Coordinate(10.0, -20.0), 5),
                Arguments.of("51DVC 65812 22723", Coordinate(-70.1, 122.1), 5),
                Arguments.of("YZJ 98062 11010", Coordinate(89.0, -179.0), 5),

                // Different precisions
                Arguments.of("27PXM 0960 0558", Coordinate(10.0, -20.0), 4),
                Arguments.of("27PXM 096 056", Coordinate(10.0, -20.0), 3),
                Arguments.of("27PXM 10 06", Coordinate(10.0, -20.0), 2),
                Arguments.of("27PXM 1 1", Coordinate(10.0, -20.0), 1),
            )
        }

        @JvmStatic
        fun provideUSNG(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("14S NJ 39521 41744", Coordinate(39.2240867222, -98.5421515000), 5),
                Arguments.of("18S UJ 23246 05745", Coordinate(38.8828019136, -77.0377807680), 5),
                Arguments.of("17R KL 70903 50491", Coordinate(27.5589270380, -83.3203125000), 5),
                Arguments.of("05Q KC 72828 79387", Coordinate(20.5998824479, -155.1796880364), 5),
            )
        }

        @JvmStatic
        fun provideOSNG(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("TG 51409 13177", Coordinate(52.657977, 1.716038), 5),
                Arguments.of("OR 96706 50582", Coordinate(55.657977, 2.716029), 5),
                Arguments.of("ST 49851 22534", Coordinate(51.0, -2.716038), 5),
                Arguments.of("SK 87290 68571", Coordinate(53.2070530000, -0.6945160000), 5),
                Arguments.of("TQ 22069 82537", Coordinate(51.5285582,-0.241681), 5),
                Arguments.of("?", Coordinate(42.1948, -71.6295), 5)
            )
        }

        @JvmStatic
        fun provideLocationStrings(): Stream<Arguments> {
            return Stream.of(
                // DDM
                Arguments.of("10°2.083333' N, 77°30.5' E", Coordinate(10.03472, 77.508333)),
                Arguments.of("10°2.083333' S, 77°30.5' E", Coordinate(-10.03472, 77.508333)),
                Arguments.of("10°2.083333' N, 77°30.5' W", Coordinate(10.03472, -77.508333)),
                Arguments.of("10°2.083333' S, 77°30.5' W", Coordinate(-10.03472, -77.508333)),
                // DMS
                Arguments.of("10°2'5.0\" N, 77°30'30.0\" E", Coordinate(10.03472, 77.508333)),
                Arguments.of("10°2'5.0\" S, 77°30'30.0\" E", Coordinate(-10.03472, 77.508333)),
                Arguments.of("10°2'5.0\" N, 77°30'30.0\" W", Coordinate(10.03472, -77.508333)),
                Arguments.of("10°2'5.0\" S, 77°30'30.0\" W", Coordinate(-10.03472, -77.508333)),
                Arguments.of("10°2'5\" S, 77°30'30\" W", Coordinate(-10.03472, -77.508333)),
                // DD
                Arguments.of("42.1948, -71.6295", Coordinate(42.1948, -71.6295)),
                Arguments.of("-90, -180", Coordinate(-90.0, -180.0)),
                Arguments.of("90, 180", Coordinate(90.0, 180.0)),
                Arguments.of("-42.1948, -71.6295", Coordinate(-42.1948, -71.6295)),
                Arguments.of("-42,1948, -71,6295", Coordinate(-42.1948, -71.6295)),
                Arguments.of("-42,1948°, -71,6295°", Coordinate(-42.1948, -71.6295)),
                Arguments.of("1.2,1.4", Coordinate(1.2, 1.4)),
                Arguments.of("1.2°, 1.4°", Coordinate(1.2, 1.4)),
                Arguments.of("1 8", Coordinate(1.0, 8.0)),
                // UTM
                Arguments.of("19T 0282888E 4674752N", Coordinate(42.1948, -71.6295)),
                Arguments.of("14T 0328056E 5290773N", Coordinate(47.7474, -101.2939)),
                Arguments.of("13R 0393008E 3051634N", Coordinate(27.5844, -106.0840)),
                Arguments.of("21L 0359923E 9098523N", Coordinate(-8.1534, -58.2715)),
                Arguments.of("34H 0674432E 6430470N", Coordinate(-32.2489, 22.8516)),
                Arguments.of("34H 0674432E 6430470N", Coordinate(-32.2489, 22.8516)),
                Arguments.of("34D 0528288E 2071725N", Coordinate(-71.4545, 21.7969)),
                Arguments.of("40X 0545559E 9051365N", Coordinate(81.5113, 59.7656)),
                Arguments.of("17M 0784692E 9999203N", Coordinate(-0.0072, -78.4424)),
                Arguments.of("09E 0353004E 3573063N", Coordinate(-57.9598, -131.4844)),
                Arguments.of("09e 0353004e 3573063n", Coordinate(-57.9598, -131.4844)),
                Arguments.of("09 E 0353004 E 3573063 N", Coordinate(-57.9598, -131.4844)),
                Arguments.of("09E 0353004 E 3573063 N", Coordinate(-57.9598, -131.4844)),
                // Polar zones UTM
                Arguments.of("A 1998062E 1888990N", Coordinate(-89.0, -179.0)),
                Arguments.of("A 1998062E 2111009N", Coordinate(-89.0, -1.0)),
                Arguments.of("B 2019279E 2109339N", Coordinate(-89.0, 10.0)),
                Arguments.of("B 2000000E 2111026N", Coordinate(-89.0, 0.0)),
                Arguments.of("B 2000000E 2000000N", Coordinate(-90.0, 0.0)),
                Arguments.of("Y 1998062E 2111009N", Coordinate(89.0, -179.0)),
                Arguments.of("Y 1998062E 1888990N", Coordinate(89.0, -1.0)),
                Arguments.of("Z 2019279E 1890660N", Coordinate(89.0, 10.0)),
                Arguments.of("Z 2000000E 1888973N", Coordinate(89.0, 0.0)),
                Arguments.of("Z 2000000E 2000000N", Coordinate(90.0, 0.0)),
                // MGRS
                Arguments.of("27PXM 09601 05579", Coordinate(10.0, -20.0)),
                Arguments.of("51DVC 65812 22723", Coordinate(-70.1, 122.1)),
                Arguments.of("51DVC6581222723", Coordinate(-70.1, 122.1)),
                Arguments.of("51 D VC 65812 22723", Coordinate(-70.1, 122.1)),
//                Arguments.of("YZJ 98062 11010", Coordinate(89.0, -179.0)), // TODO: This can be generated, but not parsed
                Arguments.of("27PXM 0960 0558", Coordinate(10.0, -20.0)),
                Arguments.of("27PXM 096 056", Coordinate(10.0, -20.0)),
                Arguments.of("27PXM 10 06", Coordinate(10.0038, -19.9963)),
                Arguments.of("27PXM 1 1", Coordinate(10.0399725934, -19.9963)),

                // USNG
                Arguments.of("14S NJ 39521 41744", Coordinate(39.2240867222, -98.5421515000)),
                Arguments.of("18S UJ 23246 05745", Coordinate(38.8828019136, -77.0377807680)),
                Arguments.of("17R KL 70903 50491", Coordinate(27.5589270380, -83.3203125000)),
                Arguments.of("05Q KC 72828 79387", Coordinate(20.5998824479, -155.1796880364)),
                Arguments.of("14SNJ3952141744", Coordinate(39.2240867222, -98.5421515000)),
                Arguments.of("18SUJ2324605745", Coordinate(38.8828019136, -77.0377807680)),
                Arguments.of("17RKL7090350491", Coordinate(27.5589270380, -83.3203125000)),
                Arguments.of("5QKC7282879387", Coordinate(20.5998824479, -155.1796880364)),

                // OSNG (OSGB-36)
                Arguments.of("TG 51409 13177", Coordinate(52.657977, 1.716038)),
                Arguments.of("OR 96706 50582", Coordinate(55.657977, 2.716029)),
                Arguments.of("ST 49851 22534", Coordinate(51.0, -2.716038)),
                Arguments.of("651409,313177", Coordinate(52.657977, 1.716038)),
                Arguments.of("696706,650582", Coordinate(55.657977, 2.716029)),
                Arguments.of("487289,368568", Coordinate(53.2070530000, -0.6945160000)),
                Arguments.of("522067,182536", Coordinate(51.5285582,-0.241681)),

                // Invalid formats / locations
                Arguments.of("91 8", null),
                Arguments.of("-91 8", null),
                Arguments.of("1 181", null),
                Arguments.of("1 -181", null),
                Arguments.of("test", null),
                Arguments.of("1 1231E 1231N", null),
                Arguments.of("1a 1231E 1231N", null),
                Arguments.of("1b 1231E 1231N", null),
                Arguments.of("1y 1231E 1231N", null),
                Arguments.of("1z 1231E 1231N", null),
                Arguments.of("1t 123", null),
                Arguments.of("12m1, 4m1", null),
                Arguments.of("61T 1234E 1234N", null),
                Arguments.of("1", null),
                Arguments.of("", null),
                Arguments.of("10°2'5.0\", 77°30'30.0\"", null),
                Arguments.of("10°2'5.0\" W, 77°30'30.0\" N", null),
                Arguments.of("10°2'5.0 S, 77°30'30.0\" W", null),
                Arguments.of("10°2'5.0 S\", 30'30.0\" W", null),
                Arguments.of("10°2' S\", 77°30'30.0\" W", null),
                Arguments.of("10°5\" S, 77°30'30.0\" W", null)

            )
        }
    }

}
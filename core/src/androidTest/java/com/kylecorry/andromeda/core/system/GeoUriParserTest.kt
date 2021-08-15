package com.kylecorry.andromeda.core.system

import android.net.Uri
import com.kylecorry.andromeda.core.units.Coordinate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.stream.Stream

internal class GeoUriParserTest {

    @Test
    fun parse() {
        val parser = GeoUriParser()

        for ((geo, expected) in provideGeo()) {
            val actual = parser.parse(geo)
            assertEquals(expected, actual)
        }
    }

    companion object {
        fun provideGeo(): Stream<Pair<Uri, GeoUriParser.NamedCoordinate?>> {
            return Stream.of(
                Pair(Uri.parse("geo:1.4,2.0"), nc(1.4, 2.0, null)),
                Pair(Uri.parse("geo:1.4,-2.0"), nc(1.4, -2.0, null)),
                Pair(Uri.parse("geo:-1.4,-2.0"), nc(-1.4, -2.0, null)),
                Pair(Uri.parse("geo:-1.4,2.0"), nc(-1.4, 2.0, null)),
                Pair(Uri.parse("geo:0.0,0.0"), nc(0.0, 0.0, null)),
                Pair(Uri.parse("geo:0.0,0.0?q=-42.5,12"), nc(-42.5, 12.0, null)),
                Pair(Uri.parse("geo:0.0,0.0?q=-42.5,12()"), nc(-42.5, 12.0, null)),
                Pair(Uri.parse("geo:0,0?q=-42.5,12(test)"), nc(-42.5, 12.0, "test")),
                Pair(Uri.parse("geo:0,0?q=-42.5,12(test+encoded)"), nc(-42.5, 12.0, "test encoded")),
                Pair(Uri.parse("geo:0,0?q=-42.5,12(test%20encoded)"), nc(-42.5, 12.0, "test encoded")),
                Pair(Uri.parse("geo:0,0?q=test+encoding"), null),
                Pair(Uri.parse("geo:1..4,2"), null),
                Pair(Uri.parse("geo:1-4,-2"), null),
                Pair(Uri.parse("geo:1-4,-2"), null),
                Pair(Uri.parse("geo:14,--21"), null),
                Pair(Uri.parse("geo:14,-..21"), null),

                )
        }

        private fun nc(lat: Double, lng: Double, name: String?): GeoUriParser.NamedCoordinate {
            return GeoUriParser.NamedCoordinate(Coordinate(lat, lng), name)
        }
    }
}
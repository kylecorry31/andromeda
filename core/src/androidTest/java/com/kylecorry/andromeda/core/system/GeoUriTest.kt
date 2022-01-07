package com.kylecorry.andromeda.core.system

import android.net.Uri
import com.kylecorry.sol.units.Coordinate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.stream.Stream

internal class GeoUriTest {

    @Test
    fun parse() {
        for ((geo, expected) in provideGeo()) {
            val actual = GeoUri.from(geo)
            assertEquals(expected, actual)
        }
    }

    companion object {
        fun provideGeo(): Stream<Pair<Uri, GeoUri?>> {
            return Stream.of(
                Pair(Uri.parse("geo:1.4,2.0"), geo(1.4, 2.0)),
                Pair(Uri.parse("geo:1.4,-2.0"), geo(1.4, -2.0)),
                Pair(Uri.parse("geo:-1.4,-2.0"), geo(-1.4, -2.0)),
                Pair(Uri.parse("geo:-1.4,2.0"), geo(-1.4, 2.0)),
                Pair(Uri.parse("geo:0.0,0.0"), geo(0.0, 0.0)),
                Pair(Uri.parse("geo:0.0,0.0?q=-42.5,12"), geo(0.0, 0.0, mapOf("q" to "-42.5,12"))),
                Pair(Uri.parse("geo:0.0,0.0?q=-42.5,12()"), geo(0.0, 0.0, mapOf("q" to "-42.5,12()"))),
                Pair(Uri.parse("geo:0,0?q=-42.5,12(test)"), geo(0.0, 0.0, mapOf("q" to "-42.5,12(test)"))),
                Pair(Uri.parse("geo:0,0?q=-42.5,12(test+encoded)"), geo(0.0, 0.0, mapOf("q" to "-42.5,12(test+encoded)"))),
                Pair(Uri.parse("geo:0,0?q=-42.5,12(test%20encoded)"), geo(0.0, 0.0, mapOf("q" to "-42.5,12(test encoded)"))),
                Pair(Uri.parse("geo:0,0?q=test+encoding&other=123"), geo(0.0, 0.0, mapOf("q" to "test+encoding", "other" to "123"))),
                Pair(Uri.parse("geo:1..4,2"), null),
                Pair(Uri.parse("geo:1-4,-2"), null),
                Pair(Uri.parse("geo:1-4,-2"), null),
                Pair(Uri.parse("geo:14,--21"), null),
                Pair(Uri.parse("geo:14,-..21"), null),

                )
        }

        private fun geo(lat: Double, lng: Double, query: Map<String, String> = mapOf()): GeoUri {
            return GeoUri(Coordinate(lat, lng), queryParameters = query)
        }
    }
}
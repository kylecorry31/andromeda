package com.kylecorry.andromeda.gpx

import com.kylecorry.sol.units.Coordinate
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.time.Instant

class GPXSerializerTest {
    @Test
    fun deserialize() {
        val data = GPXSerializer("").deserialize(gpx.byteInputStream())

        val waypoints = listOf(
            GPXWaypoint(
                Coordinate(37.778259, -122.391386),
                "Beacon 1",
                3.4f,
                description = "A test comment",
                time = Instant.parse("2016-06-17T23:41:03Z"),
                group = "Test Group"
            ),
            GPXWaypoint(Coordinate(31.0, 100.0), "Beacon 2", null, null, null, null),
        )

        val track1 = GPXTrack(
            "Test track",
            "Test type",
            1,
            "Test comment",
            segments = listOf(
                GPXTrackSegment(
                    listOf(
                        GPXWaypoint(
                            Coordinate(31.0, 100.0),
                            null,
                            -3.14f,
                            null,
                            time = Instant.parse("2016-06-17T23:41:03Z")
                        ),
                        GPXWaypoint(
                            Coordinate(32.0, 10.0),
                            null,
                            6.28f,
                            null,
                            time = Instant.parse("2017-06-17T23:41:03Z")
                        )
                    )
                ),
                GPXTrackSegment(
                    listOf(
                        GPXWaypoint(
                            Coordinate(30.0, 10.0),
                            null,
                            1f,
                            null,
                            time = Instant.parse("2017-06-17T23:41:03Z")
                        ),
                        GPXWaypoint(
                            Coordinate(31.0, 11.0),
                            null,
                            2f,
                            null,
                            time = Instant.parse("2016-06-17T23:41:03Z")
                        )
                    )
                )
            )
        )

        val track2 = GPXTrack(
            "Test track2",
            "Test type2",
            2,
            "Test comment2",
            segments = listOf(
                GPXTrackSegment(
                    listOf(
                        GPXWaypoint(
                            Coordinate(30.0, 101.0),
                            null,
                            3f,
                            null,
                            time = Instant.parse("2016-06-17T23:41:03Z")
                        ),
                        GPXWaypoint(
                            Coordinate(33.0, 11.0),
                            null,
                            6f,
                            null,
                            time = Instant.parse("2018-06-17T23:41:03Z")
                        )
                    )
                )
            )
        )

        val route = GPXRoute(
            "Test route",
            "Test desc",
            "Test cmt",
            "gps",
            3,
            "Test type",
            listOf(
                GPXWaypoint(
                    Coordinate(32.0, 101.0),
                    null,
                    3f,
                    null,
                    time = Instant.parse("2016-06-17T23:41:03Z")
                ),
                GPXWaypoint(
                    Coordinate(34.0, 11.0),
                    null,
                    6f,
                    null,
                    time = Instant.parse("2018-06-17T23:41:03Z")
                )
            )
        )

        assertEquals(
            GPXData(
                waypoints,
                listOf(
                    track1,
                    track2
                ),
                listOf(route)
            ),
            data
        )
    }

    @Test
    fun serialize() {
        val replaceRegex = ">\\s+".toRegex()
        val xml = gpx.replace(replaceRegex, ">")
        val waypoints = listOf(
            GPXWaypoint(
                Coordinate(37.778259, -122.391386),
                "Beacon 1",
                3.4f,
                description = "A test comment",
                time = Instant.parse("2016-06-17T23:41:03Z"),
                group = "Test Group"
            ),
            GPXWaypoint(Coordinate(31.0, 100.0), "Beacon 2", null, null, null, null),
        )
        val track1 = GPXTrack(
            "Test track",
            "Test type",
            1,
            "Test comment",
            segments = listOf(
                GPXTrackSegment(
                    listOf(
                        GPXWaypoint(
                            Coordinate(31.0, 100.0),
                            null,
                            -3.14f,
                            null,
                            time = Instant.parse("2016-06-17T23:41:03Z")
                        ),
                        GPXWaypoint(
                            Coordinate(32.0, 10.0),
                            null,
                            6.28f,
                            null,
                            time = Instant.parse("2017-06-17T23:41:03Z")
                        )
                    )
                ),
                GPXTrackSegment(
                    listOf(
                        GPXWaypoint(
                            Coordinate(30.0, 10.0),
                            null,
                            1f,
                            null,
                            time = Instant.parse("2017-06-17T23:41:03Z")
                        ),
                        GPXWaypoint(
                            Coordinate(31.0, 11.0),
                            null,
                            2f,
                            null,
                            time = Instant.parse("2016-06-17T23:41:03Z")
                        )
                    )
                )
            )
        )

        val track2 = GPXTrack(
            "Test track2",
            "Test type2",
            2,
            "Test comment2",
            segments = listOf(
                GPXTrackSegment(
                    listOf(
                        GPXWaypoint(
                            Coordinate(30.0, 101.0),
                            null,
                            3f,
                            null,
                            time = Instant.parse("2016-06-17T23:41:03Z")
                        ),
                        GPXWaypoint(
                            Coordinate(33.0, 11.0),
                            null,
                            6f,
                            null,
                            time = Instant.parse("2018-06-17T23:41:03Z")
                        )
                    )
                )
            )
        )

        val route = GPXRoute(
            "Test route",
            "Test desc",
            "Test cmt",
            "gps",
            3,
            "Test type",
            listOf(
                GPXWaypoint(
                    Coordinate(32.0, 101.0),
                    null,
                    3f,
                    null,
                    time = Instant.parse("2016-06-17T23:41:03Z")
                ),
                GPXWaypoint(
                    Coordinate(34.0, 11.0),
                    null,
                    6f,
                    null,
                    time = Instant.parse("2018-06-17T23:41:03Z")
                )
            )
        )

        val tracks = listOf(track1, track2)
        val stream = ByteArrayOutputStream()
        GPXSerializer("Trail Sense").serialize(GPXData(waypoints, tracks, listOf(route)), stream)

        assertEquals(xml, stream.toString())
    }


    private val gpx = """<?xml version="1.0"?>
<gpx version="1.1" creator="Trail Sense" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.topografix.com/GPX/1/1" xmlns:trailsense="https://kylecorry.com/Trail-Sense" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd https://kylecorry.com/Trail-Sense https://kylecorry.com/Trail-Sense/trailsense.xsd">
    <wpt lat="37.778259" lon="-122.391386">
        <ele>3.4</ele>
        <time>2016-06-17T23:41:03Z</time>
        <name>Beacon 1</name>
        <desc>A test comment</desc>
        <extensions>
            <trailsense:group>Test Group</trailsense:group>
        </extensions>
    </wpt>
    <wpt lat="31.0" lon="100.0">
        <name>Beacon 2</name>
    </wpt>
    <trk>
        <name>Test track</name>
        <desc>Test comment</desc>
        <number>1</number>
        <type>Test type</type>
        <trkseg>
            <trkpt lat="31.0" lon="100.0">
                <ele>-3.14</ele>
                <time>2016-06-17T23:41:03Z</time>
            </trkpt>
            <trkpt lat="32.0" lon="10.0">
                <ele>6.28</ele>
                <time>2017-06-17T23:41:03Z</time>
            </trkpt>
        </trkseg>
        <trkseg>
            <trkpt lat="30.0" lon="10.0">
                <ele>1.0</ele>
                <time>2017-06-17T23:41:03Z</time>
            </trkpt>
            <trkpt lat="31.0" lon="11.0">
                <ele>2.0</ele>
                <time>2016-06-17T23:41:03Z</time>
            </trkpt>
        </trkseg>
    </trk>
    <trk>
        <name>Test track2</name>
        <desc>Test comment2</desc>
        <number>2</number>
        <type>Test type2</type>
        <trkseg>
            <trkpt lat="30.0" lon="101.0">
                <ele>3.0</ele>
                <time>2016-06-17T23:41:03Z</time>
            </trkpt>
            <trkpt lat="33.0" lon="11.0">
                <ele>6.0</ele>
                <time>2018-06-17T23:41:03Z</time>
            </trkpt>
        </trkseg>
    </trk>
    <rte>
        <name>Test route</name>
        <cmt>Test cmt</cmt>
        <desc>Test desc</desc>
        <number>3</number>
        <type>Test type</type>
        <src>gps</src>
        <rtept lat="32.0" lon="101.0">
            <ele>3.0</ele>
            <time>2016-06-17T23:41:03Z</time>
        </rtept>
        <rtept lat="34.0" lon="11.0">
            <ele>6.0</ele>
            <time>2018-06-17T23:41:03Z</time>
        </rtept>
    </rte>
</gpx>"""


}
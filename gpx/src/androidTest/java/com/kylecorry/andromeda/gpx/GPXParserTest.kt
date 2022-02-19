package com.kylecorry.andromeda.gpx

import com.kylecorry.sol.units.Coordinate
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class GPXParserTest {
    @Test
    fun fromGPXWaypoints() {
        val data = GPXParser.parse(gpxWaypointsOnly)
        assertEquals(
            listOf(
                GPXWaypoint(
                    Coordinate(37.778259000, -122.391386000),
                    "Beacon 1",
                    3.4f,
                    "A test comment",
                    Instant.parse("2016-06-17T23:41:03Z"),
                    "Test Group"
                ),
                GPXWaypoint(Coordinate(31.0, 100.0), "Beacon 2", null, null, null, null),
            ),
            data.waypoints
        )
        assertEquals(emptyList<GPXTrack>(), data.tracks)
    }

    @Test
    fun fromGPX() {
        testFromGPX(true)
        testFromGPX(false)
    }

    private fun testFromGPX(stream: Boolean = false){
        val data = if (stream){
            GPXParser.parse(gpxWaypointsAndTracks.byteInputStream())
        } else {
            GPXParser.parse(gpxWaypointsAndTracks)
        }

        val waypoints = listOf(
            GPXWaypoint(
                Coordinate(37.778259000, -122.391386000),
                "Beacon 1",
                3.4f,
                "A test comment",
                Instant.parse("2016-06-17T23:41:03Z"),
                "Test Group"
            ),
            GPXWaypoint(Coordinate(31.0, 100.0), "Beacon 2", null, null, null, null),
        )

        val track1 = GPXTrack(
            "Test track",
            "Test type",
            1,
            "Test comment",
            listOf(
                GPXTrackSegment(
                    listOf(
                        GPXWaypoint(
                            Coordinate(31.0, 100.0),
                            null,
                            -3.14f,
                            null,
                            Instant.parse("2016-06-17T23:41:03Z"),
                            null
                        ),
                        GPXWaypoint(
                            Coordinate(32.0, 10.0),
                            null,
                            6.28f,
                            null,
                            Instant.parse("2017-06-17T23:41:03Z"),
                            null
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
                            Instant.parse("2017-06-17T23:41:03Z"),
                            null
                        ),
                        GPXWaypoint(
                            Coordinate(31.0, 11.0),
                            null,
                            2f,
                            null,
                            Instant.parse("2016-06-17T23:41:03Z"),
                            null
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
            listOf(
                GPXTrackSegment(
                    listOf(
                        GPXWaypoint(
                            Coordinate(30.0, 101.0),
                            null,
                            3f,
                            null,
                            Instant.parse("2016-06-17T23:41:03Z"),
                            null
                        ),
                        GPXWaypoint(
                            Coordinate(33.0, 11.0),
                            null,
                            6f,
                            null,
                            Instant.parse("2018-06-17T23:41:03Z"),
                            null
                        )
                    )
                )
            )
        )

        assertEquals(
            GPXData(
                waypoints,
                listOf(
                    track1,
                    track2
                )
            ),
            data
        )
    }

    @Test
    fun toGPXWaypoints() {
        val xml =
            """<?xml version="1.0"?><gpx version="1.1" creator="Trail Sense" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.topografix.com/GPX/1/1" xmlns:trailsense="https://kylecorry.com/Trail-Sense" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd https://kylecorry.com/Trail-Sense https://kylecorry.com/Trail-Sense/trailsense.xsd"><wpt lat="37.778259" lon="-122.391386"><ele>3.4</ele><time>2016-06-17T23:41:03Z</time><name>Beacon 1 &amp; 0.5</name><desc>A test comment</desc><extensions><trailsense:group>Test Group</trailsense:group></extensions></wpt><wpt lat="31.0" lon="100.0"><name>Beacon 2</name></wpt></gpx>"""
        val waypoints = listOf(
            GPXWaypoint(
                Coordinate(37.778259, -122.391386),
                "Beacon 1 & 0.5",
                3.4f,
                "A test comment",
                Instant.parse("2016-06-17T23:41:03Z"),
                "Test Group"
            ),
            GPXWaypoint(Coordinate(31.0, 100.0), "Beacon 2", null, null, null, null),
        )
        assertEquals(xml, GPXParser.toGPX(GPXData(waypoints, listOf()), "Trail Sense"))
    }

    @Test
    fun toGPX() {
        val xml =
            """<?xml version="1.0"?><gpx version="1.1" creator="Trail Sense" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.topografix.com/GPX/1/1" xmlns:trailsense="https://kylecorry.com/Trail-Sense" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd https://kylecorry.com/Trail-Sense https://kylecorry.com/Trail-Sense/trailsense.xsd"><wpt lat="37.778259" lon="-122.391386"><ele>3.4</ele><time>2016-06-17T23:41:03Z</time><name>Beacon 1 &amp; 0.5</name><desc>A test comment</desc><extensions><trailsense:group>Test Group</trailsense:group></extensions></wpt><wpt lat="31.0" lon="100.0"><name>Beacon 2</name></wpt><trk><name>Test track</name><desc>Test comment</desc><number>1</number><type>Test type</type><trkseg><trkpt lat="31.0" lon="100.0"><ele>-3.14</ele><time>2016-06-17T23:41:03Z</time></trkpt><trkpt lat="32.0" lon="10.0"><ele>6.28</ele><time>2017-06-17T23:41:03Z</time></trkpt></trkseg><trkseg><trkpt lat="30.0" lon="10.0"><ele>1.0</ele><time>2017-06-17T23:41:03Z</time></trkpt><trkpt lat="31.0" lon="11.0"><ele>2.0</ele><time>2016-06-17T23:41:03Z</time></trkpt></trkseg></trk><trk><name>Test track2</name><desc>Test comment2</desc><number>2</number><type>Test type2</type><trkseg><trkpt lat="30.0" lon="101.0"><ele>3.0</ele><time>2016-06-17T23:41:03Z</time></trkpt><trkpt lat="33.0" lon="11.0"><ele>6.0</ele><time>2018-06-17T23:41:03Z</time></trkpt></trkseg></trk></gpx>"""
        val waypoints = listOf(
            GPXWaypoint(
                Coordinate(37.778259, -122.391386),
                "Beacon 1 & 0.5",
                3.4f,
                "A test comment",
                Instant.parse("2016-06-17T23:41:03Z"),
                "Test Group"
            ),
            GPXWaypoint(Coordinate(31.0, 100.0), "Beacon 2", null, null, null, null),
        )
        val track1 = GPXTrack(
            "Test track",
            "Test type",
            1,
            "Test comment",
            listOf(
                GPXTrackSegment(
                    listOf(
                        GPXWaypoint(
                            Coordinate(31.0, 100.0),
                            null,
                            -3.14f,
                            null,
                            Instant.parse("2016-06-17T23:41:03Z"),
                            null
                        ),
                        GPXWaypoint(
                            Coordinate(32.0, 10.0),
                            null,
                            6.28f,
                            null,
                            Instant.parse("2017-06-17T23:41:03Z"),
                            null
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
                            Instant.parse("2017-06-17T23:41:03Z"),
                            null
                        ),
                        GPXWaypoint(
                            Coordinate(31.0, 11.0),
                            null,
                            2f,
                            null,
                            Instant.parse("2016-06-17T23:41:03Z"),
                            null
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
            listOf(
                GPXTrackSegment(
                    listOf(
                        GPXWaypoint(
                            Coordinate(30.0, 101.0),
                            null,
                            3f,
                            null,
                            Instant.parse("2016-06-17T23:41:03Z"),
                            null
                        ),
                        GPXWaypoint(
                            Coordinate(33.0, 11.0),
                            null,
                            6f,
                            null,
                            Instant.parse("2018-06-17T23:41:03Z"),
                            null
                        )
                    )
                )
            )
        )
        val tracks = listOf(track1, track2)
        assertEquals(xml, GPXParser.toGPX(GPXData(waypoints, tracks), "Trail Sense"))
    }


    private val gpxWaypointsOnly = """<?xml version="1.0"?>
<gpx version="1.1" creator="Trail Sense" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.topografix.com/GPX/1/1" xmlns:trailsense="https://kylecorry.com/Trail-Sense" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd https://kylecorry.com/Trail-Sense https://kylecorry.com/Trail-Sense/trailsense.xsd">
    <wpt lat="37.778259000" lon="-122.391386000">
        <ele>3.4</ele>
        <time>2016-06-17T23:41:03Z</time>
        <name>Beacon 1</name>
        <desc>A test comment</desc>
        <extensions>
            <trailsense:group>Test Group</trailsense:group>
        </extensions>
    </wpt>
    <wpt lat="31" lon="100">
        <name>Beacon 2</name>
    </wpt>
</gpx>"""

    private val gpxWaypointsAndTracks = """<?xml version="1.0"?>
<gpx version="1.1" creator="Trail Sense" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.topografix.com/GPX/1/1" xmlns:trailsense="https://kylecorry.com/Trail-Sense" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd https://kylecorry.com/Trail-Sense https://kylecorry.com/Trail-Sense/trailsense.xsd">
    <wpt lat="37.778259000" lon="-122.391386000">
        <ele>3.4</ele>
        <time>2016-06-17T23:41:03Z</time>
        <name>Beacon 1</name>
        <desc>A test comment</desc>
        <extensions>
            <trailsense:group>Test Group</trailsense:group>
        </extensions>
    </wpt>
    <wpt lat="31" lon="100">
        <name>Beacon 2</name>
    </wpt>
    <trk>
        <name>Test track</name>
        <desc>Test comment</desc>
        <number>1</number>
        <type>Test type</type>
        <trkseg>
            <trkpt lat="31" lon="100">
                <ele>-3.14</ele>
                <time>2016-06-17T23:41:03Z</time>
            </trkpt>
            <trkpt lat="32" lon="10">
                <ele>6.28</ele>
                <time>2017-06-17T23:41:03Z</time>
            </trkpt>
        </trkseg>
        <trkseg>
            <trkpt lat="30" lon="10">
                <ele>1</ele>
                <time>2017-06-17T23:41:03Z</time>
            </trkpt>
            <trkpt lat="31" lon="11">
                <ele>2</ele>
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
            <trkpt lat="30" lon="101">
                <ele>3</ele>
                <time>2016-06-17T23:41:03Z</time>
            </trkpt>
            <trkpt lat="33" lon="11">
                <ele>6</ele>
                <time>2018-06-17T23:41:03Z</time>
            </trkpt>
        </trkseg>
    </trk>
</gpx>"""


}
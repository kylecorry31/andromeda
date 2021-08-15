package com.kylecorry.andromeda.gpx

import com.kylecorry.andromeda.core.units.Coordinate
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class GPXParserTest {
    @Test
    fun fromGPX() {
        val gpx = """<?xml version="1.0"?>
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
        val waypoints = GPXParser.getWaypoints(gpx)
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
            waypoints
        )
    }

    @Test
    fun toGPX() {
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
        assertEquals(xml, GPXParser.toGPX(waypoints, "Trail Sense"))
    }


}
package com.kylecorry.andromeda.gpx

import android.text.TextUtils
import com.kylecorry.andromeda.core.math.toDoubleCompat
import com.kylecorry.andromeda.core.math.toFloatCompat
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.xml.XMLConvert
import com.kylecorry.andromeda.xml.XMLNode
import java.time.Instant

object GPXParser {

    fun toGPX(waypoints: List<GPXWaypoint>, creator: String): String {
        val children = mutableListOf<XMLNode>()
        for (waypoint in waypoints){
            children.add(toXML(waypoint))
        }

        val gpx = XMLNode(
            "gpx", mapOf(
                "version" to "1.1",
                "creator" to creator,
                "xmlns:xsi" to "http://www.w3.org/2001/XMLSchema-instance",
                "xmlns" to "http://www.topografix.com/GPX/1/1",
                "xmlns:trailsense" to "https://kylecorry.com/Trail-Sense",
                "xsi:schemaLocation" to "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd https://kylecorry.com/Trail-Sense https://kylecorry.com/Trail-Sense/trailsense.xsd"
            ),
            null,
            children
        )

        return XMLConvert.toString(gpx, true)
    }

    fun getWaypoints(gpx: String): List<GPXWaypoint> {
        val tree = try {
            XMLConvert.parse(gpx)
        } catch (e: Exception) {
            return listOf()
        }
        return tree.children.map {
            val lat =
                if (it.attributes.containsKey("lat")) it.attributes["lat"]?.toDoubleCompat() else null
            val lon =
                if (it.attributes.containsKey("lon")) it.attributes["lon"]?.toDoubleCompat() else null
            val name = it.children.firstOrNull { it.tag == "name" }?.text
            val desc = it.children.firstOrNull { it.tag == "desc" }?.text
            val time = it.children.firstOrNull { it.tag == "time" }?.text
            val ele = it.children.firstOrNull { it.tag == "ele" }?.text?.toFloatCompat()
            val extensions = it.children.firstOrNull { it.tag == "extensions" }
            val group = extensions?.children?.firstOrNull { it.tag == "trailsense:group" }?.text

            if (lat == null || lon == null) {
                return@map null
            }

            val instant = try {
                if (time == null){
                    null
                } else {
                    Instant.parse(time)
                }
            } catch (e: Exception) {
                null
            }

            return@map GPXWaypoint(Coordinate(lat, lon), name, ele, desc, instant, group)
        }.filterNotNull()
    }

    private fun toXML(waypoint: GPXWaypoint): XMLNode {
        val children = mutableListOf<XMLNode>()
        if (waypoint.elevation != null){
            children.add(XMLNode.text("ele", waypoint.elevation.toString()))
        }
        if (waypoint.time != null){
            children.add(XMLNode.text("time", waypoint.time.toString()))
        }
        if (waypoint.name != null) {
            children.add(XMLNode.text("name", TextUtils.htmlEncode(waypoint.name)))
        }
        if (waypoint.comment != null){
            children.add(XMLNode.text("desc", TextUtils.htmlEncode(waypoint.comment)))
        }
        if (waypoint.group != null){
            children.add(XMLNode("extensions", mapOf(), null, listOf(
                XMLNode.text("trailsense:group", waypoint.group)
            )))
        }

        return XMLNode("wpt", mapOf(
            "lat" to waypoint.coordinate.latitude.toString(),
            "lon" to waypoint.coordinate.longitude.toString()
        ), null, children)
    }
}
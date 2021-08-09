package com.kylecorry.andromeda.core.units

import android.location.Location
import android.os.Parcelable
import com.kylecorry.andromeda.core.math.*
import uk.gov.dstl.geo.osgb.Constants
import gov.nasa.worldwind.avlist.AVKey
import gov.nasa.worldwind.geom.Angle
import gov.nasa.worldwind.geom.coords.MGRSCoord
import gov.nasa.worldwind.geom.coords.UPSCoord
import gov.nasa.worldwind.geom.coords.UTMCoord
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import uk.gov.dstl.geo.osgb.EastingNorthingConversion
import uk.gov.dstl.geo.osgb.NationalGrid
import uk.gov.dstl.geo.osgb.OSGB36
import java.util.*
import kotlin.math.*


@Parcelize
data class Coordinate(val latitude: Double, val longitude: Double) : Parcelable {

    @IgnoredOnParcel
    val isNorthernHemisphere = latitude > 0

    override fun toString(): String {
        return toDecimalDegrees()
    }

    fun distanceTo(other: Coordinate): Float {
        val results = FloatArray(3)
        Location.distanceBetween(latitude, longitude, other.latitude, other.longitude, results)
        return results[0]
    }

    fun plus(distance: Distance, bearing: Bearing): Coordinate {
        return plus(distance.meters().distance.toDouble(), bearing)
    }

    fun plus(meters: Double, bearing: Bearing): Coordinate {
        // Adapted from https://www.movable-type.co.uk/scripts/latlong.html
        val radius = 6371e3
        val newLat = asin(
            sinDegrees(latitude) * cos(meters / radius) +
                    cosDegrees(latitude) * sin(meters / radius) * cosDegrees(bearing.value.toDouble())
        ).toDegrees()

        val newLng = longitude + atan2(
            sinDegrees(bearing.value.toDouble()) * sin(meters / radius) * cosDegrees(latitude),
            cos(meters / radius) - sinDegrees(latitude) * sinDegrees(newLat)
        ).toDegrees()

        val normalLng = (newLng + 540) % 360 - 180

        return Coordinate(newLat, normalLng)
    }

    fun toDecimalDegrees(precision: Int = 6): String {
        val formattedLatitude = DecimalFormatter.format(latitude, precision)
        val formattedLongitude = DecimalFormatter.format(longitude, precision)
        return "$formattedLatitude°,  $formattedLongitude°"
    }

    fun toDegreeDecimalMinutes(precision: Int = 3): String {
        val latDir = if (latitude < 0) "S" else "N"
        val lngDir = if (longitude < 0) "W" else "E"
        return "${ddmString(latitude, precision)}$latDir    ${
            ddmString(
                longitude,
                precision
            )
        }$lngDir"
    }

    fun toDegreeMinutesSeconds(precision: Int = 1): String {
        val latDir = if (latitude < 0) "S" else "N"
        val lngDir = if (longitude < 0) "W" else "E"
        return "${dmsString(latitude, precision)}${latDir}    ${
            dmsString(
                longitude,
                precision
            )
        }${lngDir}"
    }

    fun toUSNG(precision: Int = 5): String {
        val mgrs = toMGRS(precision)
        if (mgrs.length > 3) {
            return mgrs.substring(0, 3) + " " + mgrs.substring(3)
        }
        return mgrs
    }

    fun toMGRS(precision: Int = 5): String {
        return try {
            val lat = Angle.fromDegreesLatitude(latitude)
            val lng = Angle.fromDegreesLongitude(longitude)
            val mgrs = MGRSCoord.fromLatLon(lat, lng, precision)
            mgrs.toString().trim()
        } catch (e: Exception) {
            "?"
        }
    }

    fun toUTM(precision: Int = 7): String {
        try {
            val lat = Angle.fromDegreesLatitude(latitude)
            val lng = Angle.fromDegreesLongitude(longitude)
            val utm = UTMCoord.fromLatLon(lat, lng)

            val zone = utm.zone.toString().padStart(2, '0')

            val letter =
                if (latitude < -72) 'C' else if (latitude < -64) 'D' else if (latitude < -56) 'E' else if (latitude < -48) 'F' else if (latitude < -40) 'G' else if (latitude < -32) 'H' else if (latitude < -24) 'J' else if (latitude < -16) 'K' else if (latitude < -8) 'L' else if (latitude < 0) 'M' else if (latitude < 8) 'N' else if (latitude < 16) 'P' else if (latitude < 24) 'Q' else if (latitude < 32) 'R' else if (latitude < 40) 'S' else if (latitude < 48) 'T' else if (latitude < 56) 'U' else if (latitude < 64) 'V' else if (latitude < 72) 'W' else 'X'


            val easting =
                roundUTMPrecision(precision, utm.easting.toInt()).toString().padStart(7, '0') + "E"
            val northing =
                roundUTMPrecision(precision, utm.northing.toInt()).toString().padStart(7, '0') + "N"

            return "$zone$letter $easting $northing"
        } catch (e: Exception) {
            return toUPS(precision)
        }
    }

    // TODO: Support precision
    fun toOSNG(precision: Int = 5): String {
        try {
            val osgb36 = OSGB36.fromWGS84(latitude, longitude)
            val en = EastingNorthingConversion.fromLatLon(
                osgb36,
                Constants.ELLIPSOID_AIRY1830_MAJORAXIS,
                Constants.ELLIPSOID_AIRY1830_MINORAXIS,
                Constants.NATIONALGRID_N0,
                Constants.NATIONALGRID_E0,
                Constants.NATIONALGRID_F0,
                Constants.NATIONALGRID_LAT0,
                Constants.NATIONALGRID_LON0
            )
            val ng = NationalGrid.toNationalGrid(en)
            if (ng.isPresent) {
                return ng.get()
            }
        } catch (e: Exception) {
            return "?"
        }
        return "?"
    }

    private fun toUPS(precision: Int = 7): String {
        try {
            val lat = Angle.fromDegreesLatitude(latitude)
            val lng = Angle.fromDegreesLongitude(longitude)
            val ups = UPSCoord.fromLatLon(lat, lng)

            val easting =
                roundUTMPrecision(precision, ups.easting.toInt()).toString().padStart(7, '0') + "E"
            val northing =
                roundUTMPrecision(precision, ups.northing.toInt()).toString().padStart(7, '0') + "N"

            val letter = if (isNorthernHemisphere) {
                if (latitude == 90.0 || longitude >= 0) {
                    'Z'
                } else {
                    'Y'
                }
            } else {
                if (latitude == -90.0 || longitude >= 0) {
                    'B'
                } else {
                    'A'
                }
            }

            return "$letter $easting $northing"
        } catch (e: Exception) {
            return "?"
        }
    }

    private fun roundUTMPrecision(precision: Int, utmValue: Int): Int {
        return (utmValue / power(10.0, 7 - precision)).toInt() * power(10.0, 7 - precision).toInt()
    }

    /**
     * Get the bearing to the other coordinate (using True North)
     */
    fun bearingTo(other: Coordinate): Bearing {
        val results = FloatArray(3)
        Location.distanceBetween(latitude, longitude, other.latitude, other.longitude, results)
        return Bearing(results[1])
    }

    private fun ddmString(degrees: Double, precision: Int = 3): String {
        val deg = abs(degrees.toInt())
        val minutes = abs((degrees % 1) * 60).roundPlaces(precision)
        return "$deg°$minutes'"
    }

    private fun dmsString(degrees: Double, precision: Int = 1): String {
        val deg = abs(degrees.toInt())
        val minutes = abs((degrees % 1) * 60)
        val seconds = abs(((minutes % 1) * 60).roundPlaces(precision))
        return "$deg°${minutes.toInt()}'$seconds\""
    }

    companion object {

        val zero = Coordinate(0.0, 0.0)

        fun parse(location: String, format: CoordinateFormat? = null): Coordinate? {
            if (format == null) {
                for (fmt in CoordinateFormat.values()) {
                    val parsed = parse(location, fmt)
                    if (parsed != null) {
                        return parsed
                    }
                }
                return null
            }

            return when (format) {
                CoordinateFormat.DecimalDegrees -> fromDecimalDegrees(location)
                CoordinateFormat.DegreesDecimalMinutes -> fromDegreesDecimalMinutes(location)
                CoordinateFormat.DegreesMinutesSeconds -> fromDegreesMinutesSeconds(location)
                CoordinateFormat.UTM -> fromUTM(location)
                CoordinateFormat.MGRS -> fromMGRS(location)
                CoordinateFormat.USNG -> fromMGRS(location)
                CoordinateFormat.OSNG_OSGB36 -> fromOSNG(location)
            }
        }

        private fun fromMGRS(location: String): Coordinate? {
            return try {
                val mgrs = MGRSCoord.fromString(location)
                Coordinate(mgrs.latitude.degrees, mgrs.longitude.degrees)
            } catch (e: Exception) {
                null
            }
        }

        private fun fromOSNG(location: String): Coordinate? {
            return try {
                val eastingNorthing = try {
                    NationalGrid.fromNationalGrid(location)
                } catch (e: Exception) {
                    val split = location.split(",")
                    val en = split.mapNotNull { it.toDoubleCompat() }.toDoubleArray()
                    NationalGrid.toNationalGrid(en)
                    en
                }
                val latlonOSGB = EastingNorthingConversion.toLatLon(
                    eastingNorthing,
                    Constants.ELLIPSOID_AIRY1830_MAJORAXIS,
                    Constants.ELLIPSOID_AIRY1830_MINORAXIS,
                    Constants.NATIONALGRID_N0,
                    Constants.NATIONALGRID_E0,
                    Constants.NATIONALGRID_F0,
                    Constants.NATIONALGRID_LAT0,
                    Constants.NATIONALGRID_LON0
                )
                val latlonWGS84 = OSGB36.toWGS84(latlonOSGB[0], latlonOSGB[1])
                return Coordinate(latlonWGS84[0], latlonWGS84[1])
            } catch (e: Exception) {
                null
            }
        }

        private fun fromDecimalDegrees(location: String): Coordinate? {
            val regex = Regex("^(-?\\d+(?:[.,]\\d+)?)°?[,\\s]+(-?\\d+(?:[.,]\\d+)?)°?\$")
            val matches = regex.find(location.trim()) ?: return null
            val latitude = matches.groupValues[1].toDoubleCompat() ?: return null
            val longitude = matches.groupValues[2].toDoubleCompat() ?: return null

            if (isValidLatitude(latitude) && isValidLongitude(longitude)) {
                return Coordinate(latitude, longitude)
            }

            return null
        }

        private fun fromDegreesDecimalMinutes(location: String): Coordinate? {
            val ddmRegex =
                Regex("^(\\d+)°\\s*(\\d+(?:[.,]\\d+)?)'\\s*([nNsS])[,\\s]+(\\d+)°\\s*(\\d+(?:[.,]\\d+)?)'\\s*([wWeE])\$")
            val matches = ddmRegex.find(location.trim()) ?: return null

            var latitudeDecimal = 0.0
            latitudeDecimal += matches.groupValues[1].toDouble()
            latitudeDecimal += (matches.groupValues[2].toDoubleCompat() ?: 0.0) / 60
            latitudeDecimal *= if (matches.groupValues[3].toLowerCase(Locale.getDefault()) == "n") 1 else -1

            var longitudeDecimal = 0.0
            longitudeDecimal += matches.groupValues[4].toDouble()
            longitudeDecimal += (matches.groupValues[5].toDoubleCompat() ?: 0.0) / 60
            longitudeDecimal *= if (matches.groupValues[6].toLowerCase(Locale.getDefault()) == "e") 1 else -1

            if (isValidLatitude(latitudeDecimal) && isValidLongitude(longitudeDecimal)) {
                return Coordinate(latitudeDecimal, longitudeDecimal)
            }

            return null
        }

        private fun fromDegreesMinutesSeconds(location: String): Coordinate? {
            val dmsRegex =
                Regex("^(\\d+)°\\s*(\\d+)'\\s*(\\d+(?:[.,]\\d+)?)\"\\s*([nNsS])[,\\s]+(\\d+)°\\s*(\\d+)'\\s*(\\d+(?:[.,]\\d+)?)\"\\s*([wWeE])\$")
            val matches = dmsRegex.find(location.trim()) ?: return null

            var latitudeDecimal = 0.0
            latitudeDecimal += matches.groupValues[1].toDouble()
            latitudeDecimal += matches.groupValues[2].toDouble() / 60
            latitudeDecimal += (matches.groupValues[3].toDoubleCompat() ?: 0.0) / (60 * 60)
            latitudeDecimal *= if (matches.groupValues[4].toLowerCase(Locale.getDefault()) == "n") 1 else -1

            var longitudeDecimal = 0.0
            longitudeDecimal += matches.groupValues[5].toDouble()
            longitudeDecimal += matches.groupValues[6].toDouble() / 60
            longitudeDecimal += (matches.groupValues[7].toDoubleCompat() ?: 0.0) / (60 * 60)
            longitudeDecimal *= if (matches.groupValues[8].toLowerCase(Locale.getDefault()) == "e") 1 else -1

            if (isValidLatitude(latitudeDecimal) && isValidLongitude(longitudeDecimal)) {
                return Coordinate(latitudeDecimal, longitudeDecimal)
            }

            return null
        }

        private fun fromUTM(utm: String): Coordinate? {
            val regex =
                Regex("(\\d*)\\s*([a-z,A-Z^ioIO])\\s*(\\d+(?:[.,]\\d+)?)[\\smMeE]+(\\d+(?:[.,]\\d+)?)[\\smMnN]*")
            val matches = regex.find(utm) ?: return null

            val zone = matches.groupValues[1].toIntOrNull() ?: 0
            val letter = matches.groupValues[2].toCharArray().first()
            val easting = matches.groupValues[3].toDoubleCompat() ?: 0.0
            val northing = matches.groupValues[4].toDoubleCompat() ?: 0.0

            return fromUTM(zone, letter, easting, northing)
        }

        private fun fromUTM(
            zone: Int,
            letter: Char,
            easting: Double,
            northing: Double
        ): Coordinate? {
            val polarLetters = listOf('A', 'B', 'Y', 'Z')
            return try {
                if (polarLetters.contains(letter.uppercaseChar())) {
                    // Get it into the catch block
                    throw Exception()
                }
                val latLng = UTMCoord.locationFromUTMCoord(
                    zone,
                    if (letter.uppercaseChar() <= 'M') AVKey.SOUTH else AVKey.NORTH,
                    easting,
                    northing
                )
                Coordinate(latLng.latitude.degrees, latLng.longitude.degrees)
            } catch (e: Exception) {
                val letters = listOf('A', 'B', 'Y', 'Z')
                if (zone != 0 || !letters.contains(letter.uppercaseChar())) {
                    return null
                }
                try {
                    val latLng = UPSCoord.fromUPS(
                        if (letter.uppercaseChar() <= 'M') AVKey.SOUTH else AVKey.NORTH,
                        easting,
                        northing
                    )
                    Coordinate(latLng.latitude.degrees, latLng.longitude.degrees)
                } catch (e2: Exception) {
                    null
                }
            }
        }

        private fun isValidLongitude(longitude: Double): Boolean {
            return longitude.absoluteValue <= 180
        }

        private fun isValidLatitude(latitude: Double): Boolean {
            return latitude.absoluteValue <= 90
        }
    }
}
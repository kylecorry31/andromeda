package com.kylecorry.andromeda.sense.location

import androidx.core.location.GnssStatusCompat

data class Satellite(
    val id: Int,
    val constellation: SatelliteConstellation?,
    val elevation: Float,
    val azimuth: Float,
    val usedInFix: Boolean
) {
    companion object {

        fun fromStatus(status: GnssStatusCompat): List<Satellite> {
            val satellites = mutableListOf<Satellite>()
            for (i in 0 until status.satelliteCount) {
                satellites.add(fromStatus(status, i))
            }
            return satellites
        }

        fun fromStatus(status: GnssStatusCompat, index: Int): Satellite {
            return Satellite(
                index,
                when (status.getConstellationType(index)) {
                    GnssStatusCompat.CONSTELLATION_GPS -> SatelliteConstellation.GPS
                    GnssStatusCompat.CONSTELLATION_SBAS -> SatelliteConstellation.SBAS
                    GnssStatusCompat.CONSTELLATION_GLONASS -> SatelliteConstellation.GLONASS
                    GnssStatusCompat.CONSTELLATION_QZSS -> SatelliteConstellation.QZSS
                    GnssStatusCompat.CONSTELLATION_BEIDOU -> SatelliteConstellation.BEIDOU
                    GnssStatusCompat.CONSTELLATION_GALILEO -> SatelliteConstellation.GALILEO
                    GnssStatusCompat.CONSTELLATION_IRNSS -> SatelliteConstellation.IRNSS
                    else -> null
                },
                status.getElevationDegrees(index),
                status.getAzimuthDegrees(index),
                status.usedInFix(index)
            )
        }
    }
}

enum class SatelliteConstellation {
    GPS,
    SBAS,
    GLONASS,
    QZSS,
    BEIDOU,
    GALILEO,
    IRNSS
}

package com.kylecorry.andromeda.sense.location

import com.kylecorry.andromeda.core.tryOrDefault

data class Nmea(val message: String) {
    val mslAltitude: Float?
        get() {
            val validProtocols = listOf("\$GPGGA", "\$GNGNS", "\$GNGGA")
            val containsMslAltitude = validProtocols.any { message.startsWith(it) }

            if (!containsMslAltitude) {
                return null
            }

            val idx = 9
            return tryOrDefault(null) {
                val altitudeStr = message.split(",")[idx]
                altitudeStr.toFloatOrNull()
            }
        }
}
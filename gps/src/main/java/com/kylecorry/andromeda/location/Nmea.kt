package com.kylecorry.andromeda.location

data class Nmea(val message: String) {
    val mslAltitude: Float?
        get() {
            val validProtocols = listOf("\$GPGGA", "\$GNGNS", "\$GNGGA")
            val containsMslAltitude = validProtocols.any { message.startsWith(it) }

            if (!containsMslAltitude){
                return null
            }

            val idx = 9
            return try {
                val altitudeStr = message.split(",")[idx]
                altitudeStr.toFloatOrNull()
            } catch (e: Exception){
                null
            }

        }
}
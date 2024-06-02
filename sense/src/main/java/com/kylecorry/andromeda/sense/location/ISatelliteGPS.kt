package com.kylecorry.andromeda.sense.location

interface ISatelliteGPS : IGPS {
    /**
     * The number of satellites used to calculate the location
     */
    val satellites: Int?

    /**
     * The details of the satellites detected by the GNSS receiver
     */
    val satelliteDetails: List<Satellite>?
}
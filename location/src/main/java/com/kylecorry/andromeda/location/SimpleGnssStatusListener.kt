package com.kylecorry.andromeda.location

import androidx.core.location.GnssStatusCompat

internal class SimpleGnssStatusListener(private val onStatusChanged: (GnssStatusCompat) -> Unit) :
    GnssStatusCompat.Callback() {

    override fun onSatelliteStatusChanged(status: GnssStatusCompat) {
        onStatusChanged(status)
    }
}
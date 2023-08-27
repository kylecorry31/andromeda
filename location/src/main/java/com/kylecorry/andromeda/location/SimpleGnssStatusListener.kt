package com.kylecorry.andromeda.location

import androidx.core.location.GnssStatusCompat

internal class SimpleGnssStatusListener(
    private val onStatusChanged: (GnssStatusCompat, Boolean) -> Unit
) : GnssStatusCompat.Callback() {

    private var hasFix = false

    override fun onStarted() {
        super.onStarted()
        hasFix = false
    }

    override fun onStopped() {
        super.onStopped()
        hasFix = false
    }

    override fun onFirstFix(ttffMillis: Int) {
        super.onFirstFix(ttffMillis)
        hasFix = true
    }

    override fun onSatelliteStatusChanged(status: GnssStatusCompat) {
        onStatusChanged(status, hasFix)
    }
}
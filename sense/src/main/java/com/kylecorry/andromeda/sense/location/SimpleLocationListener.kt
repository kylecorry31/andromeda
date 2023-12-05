package com.kylecorry.andromeda.sense.location

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

internal class SimpleLocationListener(private val onLocationChangedFn: (location: Location?) -> Unit): LocationListener {
    override fun onLocationChanged(location: Location) {
        onLocationChangedFn.invoke(location)
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderDisabled(provider: String) {
    }

    override fun onProviderEnabled(provider: String) {
    }
}
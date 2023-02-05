package com.kylecorry.andromeda.location

import android.location.Location
import android.location.LocationListener

internal class SimpleLocationListener(private val onLocationChangedFn: (location: Location?) -> Unit): LocationListener {
    override fun onLocationChanged(location: Location) {
        onLocationChangedFn.invoke(location)
    }

    override fun onProviderDisabled(provider: String) {
    }

    override fun onProviderEnabled(provider: String) {

    }
}
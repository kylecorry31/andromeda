package com.kylecorry.andromeda.sense.location

import android.content.Context
import android.location.LocationManager

class NetworkGPS(
    context: Context,
    requestConfig: LocationRequestConfig = LocationRequestConfig()
) : BaseGPS(
    context,
    LocationManager.NETWORK_PROVIDER,
    requestConfig
), IGPS {
    companion object {
        fun isAvailable(context: Context): Boolean {
            return isAvailable(context, LocationManager.NETWORK_PROVIDER)
        }
    }
}

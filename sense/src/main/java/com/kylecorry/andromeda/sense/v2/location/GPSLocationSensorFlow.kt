package com.kylecorry.andromeda.sense.v2.location

import android.content.Context
import android.location.LocationManager
import com.kylecorry.andromeda.sense.location.LocationRequestConfig

class GPSLocationSensorFlow(
    context: Context,
    requestConfig: LocationRequestConfig = LocationRequestConfig()
) : LocationSensorFlow(context, LocationManager.GPS_PROVIDER, requestConfig) {
    companion object {
        fun isAvailable(context: Context) =
            LocationSensorFlow.isProviderAvailable(context, LocationManager.GPS_PROVIDER)
    }
}

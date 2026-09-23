package com.kylecorry.andromeda.sense.v2.location

import android.content.Context
import android.location.LocationManager
import com.kylecorry.andromeda.sense.location.LocationRequestConfig

class PassiveLocationSensorFlow(
    context: Context,
    requestConfig: LocationRequestConfig = LocationRequestConfig()
) : LocationSensorFlow(context, LocationManager.PASSIVE_PROVIDER, requestConfig) {
    companion object {
        fun isAvailable(context: Context) =
            LocationSensorFlow.isProviderAvailable(context, LocationManager.PASSIVE_PROVIDER)
    }
}

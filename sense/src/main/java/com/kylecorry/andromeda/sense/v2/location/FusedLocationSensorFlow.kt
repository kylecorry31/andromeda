package com.kylecorry.andromeda.sense.v2.location

import android.content.Context
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.kylecorry.andromeda.sense.location.LocationRequestConfig

@RequiresApi(Build.VERSION_CODES.S)
class FusedLocationSensorFlow(
    context: Context,
    requestConfig: LocationRequestConfig = LocationRequestConfig()
) : LocationSensorFlow(context, LocationManager.FUSED_PROVIDER, requestConfig) {
    companion object {
        fun isAvailable(context: Context) =
            LocationSensorFlow.isProviderAvailable(context, LocationManager.FUSED_PROVIDER)
    }
}

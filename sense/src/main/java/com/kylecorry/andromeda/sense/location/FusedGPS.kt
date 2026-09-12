package com.kylecorry.andromeda.sense.location

import android.content.Context
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.S)
class FusedGPS(
    context: Context,
    requestConfig: LocationRequestConfig = LocationRequestConfig()
) : BaseGPS(
    context,
    LocationManager.FUSED_PROVIDER,
    requestConfig
), IGPS

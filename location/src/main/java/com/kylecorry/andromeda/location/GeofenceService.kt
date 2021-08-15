package com.kylecorry.andromeda.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.location.LocationManager
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.andromeda.permissions.PermissionService
import java.time.Duration

class GeofenceService(private val context: Context) {

    private val locationManager by lazy { context.getSystemService<LocationManager>() }

    @SuppressLint("MissingPermission")
    fun addGeofence(
        location: Coordinate,
        radius: Distance,
        pendingIntent: PendingIntent,
        expiration: Duration? = null
    ) {
        val permissions = PermissionService(context)
        if (permissions.canGetFineLocation()) {
            locationManager?.addProximityAlert(
                location.latitude,
                location.longitude,
                radius.meters().distance,
                expiration?.toMillis() ?: -1,
                pendingIntent
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun removeGeofence(pendingIntent: PendingIntent) {
        val permissions = PermissionService(context)
        if (permissions.canGetFineLocation()) {
            locationManager?.removeProximityAlert(pendingIntent)
        }
    }

}
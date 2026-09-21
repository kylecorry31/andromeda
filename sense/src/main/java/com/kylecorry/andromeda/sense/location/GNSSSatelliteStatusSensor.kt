package com.kylecorry.andromeda.sense.location

import android.content.Context
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import androidx.core.location.LocationManagerCompat
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.permissions.Permissions

class GNSSSatelliteStatusSensor(private val context: Context) : AbstractSensor(),
    ISatelliteStatusSensor {
    override var satellites: Int? = null
        private set

    override var satelliteDetails: List<Satellite>? = null
        private set

    private val gnssListener = SimpleGnssStatusListener { status ->
        satelliteDetails = Satellite.fromStatus(status)
        satellites = satelliteDetails?.count { it.usedInFix }
        notifyListeners()
    }

    private val locationManager by lazy { context.getSystemService<LocationManager>() }

    @RequiresPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    override fun startImpl() {
        satellites = null
        satelliteDetails = null

        if (Permissions.canGetFineLocation(context)) {
            locationManager?.let {
                LocationManagerCompat.registerGnssStatusCallback(
                    it,
                    gnssListener,
                    Handler(Looper.getMainLooper())
                )
            }
        }
    }

    override fun stopImpl() {
        locationManager?.let {
            LocationManagerCompat.unregisterGnssStatusCallback(it, gnssListener)
        }
    }

    override val hasValidReading: Boolean
        get() = satellites != null
}

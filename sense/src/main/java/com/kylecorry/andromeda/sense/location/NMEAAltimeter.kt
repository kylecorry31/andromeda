package com.kylecorry.andromeda.sense.location

import android.content.Context
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.sensors.IAltimeter
import com.kylecorry.andromeda.permissions.Permissions

class NMEAAltimeter(private val context: Context) : AbstractSensor(), IAltimeter {

    override var altitude: Float = 0f
        private set

    override var hasValidReading: Boolean = false
        private set

    private val locationManager by lazy { context.getSystemService<LocationManager>() }

    private val nmeaListener by lazy {
        SimpleNmeaListener {
            updateNmeaString(it)
        }
    }

    @RequiresPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    override fun startImpl() {
        if (Permissions.canGetFineLocation(context)) {
            locationManager?.addNmeaListener(nmeaListener, Handler(Looper.getMainLooper()))
        }
    }

    override fun stopImpl() {
        locationManager?.removeNmeaListener(nmeaListener)
    }

    private fun updateNmeaString(message: String) {
        val nmea = Nmea(message)
        if (nmea.mslAltitude != null) {
            altitude = nmea.mslAltitude ?: 0f
            hasValidReading = true
            notifyListeners()
        }
    }
}

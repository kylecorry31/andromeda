package com.kylecorry.andromeda.sense.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.location.LocationManagerCompat
import com.kylecorry.andromeda.core.tryOrNothing
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.sol.units.Coordinate

@SuppressLint("MissingPermission")
class GPS(
    context: Context,
    requestConfig: LocationRequestConfig = LocationRequestConfig(),
    private val notifyNmeaChanges: Boolean = false,
    private val listenToNmea: Boolean = true,
    private val notifyGnssStatusChanges: Boolean = false,
    private val listenToGnssStatusChanges: Boolean = true,
) : BaseGPS(
    context,
    LocationManager.GPS_PROVIDER,
    requestConfig
),
    ISatelliteGPS {

    override val hasValidReading: Boolean
        get() = location != Coordinate.zero

    override val satellites: Int?
        get() = gnssSatellites ?: locationSatellites

    override var satelliteDetails: List<Satellite>? = null
        private set
    private val nmeaListener by lazy {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            SimpleNmeaListener {
                updateNmeaString(it)
            }
        } else {
            null
        }
    }
    private val legacyNmeaListener = SimpleLegacyNmeaListener {
        updateNmeaString(it)
    }

    private val gnssListener = SimpleGnssStatusListener { status ->
        satelliteDetails = Satellite.fromStatus(status)
        gnssSatellites = satelliteDetails?.count { it.usedInFix }
        if (notifyGnssStatusChanges) notifyListeners()
    }

    private var locationSatellites: Int? = null
    private var gnssSatellites: Int? = null

    override fun startImpl() {
        gnssSatellites = null
        satelliteDetails = null

        super.startImpl()

        // Can only get NMEA with fine location permission
        if (listenToNmea && Permissions.canGetFineLocation(context)) {
            tryOrNothing {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    nmeaListener?.let {
                        locationManager?.addNmeaListener(it, Handler(Looper.getMainLooper()))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    locationManager?.addNmeaListener(legacyNmeaListener)
                }
            }
        }

        // Listen to the GNSS status for satellite count
        if (listenToGnssStatusChanges && Permissions.canGetFineLocation(context)) {
            tryOrNothing {
                locationManager?.let {
                    LocationManagerCompat.registerGnssStatusCallback(
                        it,
                        gnssListener,
                        Handler(Looper.getMainLooper())
                    )
                }
            }
        }
    }

    override fun stopImpl() {
        super.stopImpl()

        tryOrNothing {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                nmeaListener?.let { locationManager?.removeNmeaListener(it) }
            } else {
                @Suppress("DEPRECATION")
                locationManager?.removeNmeaListener(legacyNmeaListener)
            }
        }

        tryOrNothing {
            locationManager?.let {
                LocationManagerCompat.unregisterGnssStatusCallback(it, gnssListener)
            }
        }
    }

    private fun updateNmeaString(message: String) {
        val nmea = Nmea(message)
        if (nmea.mslAltitude != null) {
            mslAltitude = nmea.mslAltitude
            if (notifyNmeaChanges) notifyListeners()
        }
    }

    override fun updateLastLocation(location: Location?, notify: Boolean) {
        if (location != null) {
            locationSatellites = if (location.extras?.containsKey("satellites") == true) {
                location.extras?.getInt("satellites")
            } else {
                null
            }
        }

        super.updateLastLocation(location, notify)
    }

    companion object {
        fun isAvailable(context: Context): Boolean {
            return BaseGPS.isAvailable(context, LocationManager.GPS_PROVIDER)
        }
    }
}

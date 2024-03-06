package com.kylecorry.andromeda.sense.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.getSystemService
import androidx.core.location.LocationCompat
import androidx.core.location.LocationManagerCompat
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.sensors.Quality
import com.kylecorry.andromeda.core.tryOrDefault
import com.kylecorry.andromeda.core.tryOrNothing
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.sol.units.*
import java.time.Duration
import java.time.Instant

@SuppressLint("MissingPermission")
class GPS(
    private val context: Context,
    private val notifyNmeaChanges: Boolean = false,
    private val notifyGnssStatusChanges: Boolean = false,
    private val frequency: Duration = Duration.ofSeconds(20),
    private val minDistance: Distance = Distance.meters(0f)
) : AbstractSensor(),
    IGPS {

    override val hasValidReading: Boolean
        get() = location != Coordinate.zero

    override val satellites: Int?
        get() = _gnssSatellites ?: _satellites

    override val quality: Quality
        get() = _quality

    override val horizontalAccuracy: Float?
        get() = _horizontalAccuracy

    override val verticalAccuracy: Float?
        get() = _verticalAccuracy

    override val location: Coordinate
        get() = _location

    override val speed: Speed
        get() = Speed(_speed, DistanceUnits.Meters, TimeUnits.Seconds)

    override val time: Instant
        get() = _time

    override val altitude: Float
        get() = _altitude

    override val mslAltitude: Float?
        get() = _mslAltitude
    override val bearing: Bearing?
        get() = _bearing?.let { Bearing(it) }
    override val rawBearing: Float?
        get() = _bearing
    override val bearingAccuracy: Float?
        get() = _bearingAccuracy
    override val speedAccuracy: Float?
        get() = _speedAccuracy
    override var fixTimeElapsedNanos: Long? = null
        private set

    private val locationManager by lazy { context.getSystemService<LocationManager>() }
    private val locationListener = SimpleLocationListener { updateLastLocation(it, true) }
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

    private val gnssListener = SimpleGnssStatusListener { status, hasFix ->

        if (!hasFix) {
            _gnssSatellites = 0
        }

        val satellitesUsedInFix = (0 until status.satelliteCount).count { s ->
            status.usedInFix(s)
        }

        _gnssSatellites = satellitesUsedInFix

        if (notifyGnssStatusChanges) notifyListeners()
    }

    private var _altitude = 0f
    private var _time = Instant.now()
    private var _quality = Quality.Unknown
    private var _horizontalAccuracy: Float? = null
    private var _verticalAccuracy: Float? = null
    private var _satellites: Int? = null
    private var _gnssSatellites: Int? = null
    private var _speed: Float = 0f
    private var _speedAccuracy: Float? = null
    private var _bearing: Float? = null
    private var _bearingAccuracy: Float? = null
    private var _location = Coordinate.zero
    private var _mslAltitude: Float? = null

    init {
        tryOrNothing {
            if (Permissions.canGetLocation(context)) {
                updateLastLocation(
                    locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER),
                    false
                )
            }
        }
    }

    override fun startImpl() {
        if (!Permissions.canGetLocation(context)) {
            return
        }

        updateLastLocation(
            locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER),
            false
        )

        _gnssSatellites = null

        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            frequency.toMillis(),
            minDistance.meters().distance,
            locationListener,
            Looper.getMainLooper()
        )

        // Can only get NMEA with fine location permission
        if (Permissions.canGetFineLocation(context)) {
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
        if (Permissions.canGetFineLocation(context)) {
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
        locationManager?.removeUpdates(locationListener)
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
            _mslAltitude = nmea.mslAltitude
            if (notifyNmeaChanges) notifyListeners()
        }
    }

    private fun updateLastLocation(location: Location?, notify: Boolean = true) {
        if (location == null) {
            return
        }

        _location = Coordinate(location.latitude, location.longitude)
        _time = Instant.ofEpochMilli(location.time)
        fixTimeElapsedNanos = location.elapsedRealtimeNanos

        _satellites = if (location.extras?.containsKey("satellites") == true) {
            location.extras?.getInt("satellites")
        } else {
            null
        }

        _altitude = if (location.hasAltitude()) location.altitude.toFloat() else 0f
        val accuracy = if (location.hasAccuracy()) location.accuracy else null
        _quality = when {
            accuracy != null && accuracy < 8 -> Quality.Good
            accuracy != null && accuracy < 16 -> Quality.Moderate
            accuracy != null -> Quality.Poor
            else -> Quality.Unknown
        }
        _horizontalAccuracy = accuracy ?: 0f
        _verticalAccuracy = if (LocationCompat.hasVerticalAccuracy(location)) {
            LocationCompat.getVerticalAccuracyMeters(location)
        } else {
            null
        }

        _speedAccuracy = if (LocationCompat.hasSpeedAccuracy(location)) {
            LocationCompat.getSpeedAccuracyMetersPerSecond(location)
        } else {
            null
        }

        _speed = if (location.hasSpeed()) {
            val currentSpeedAccuracy = _speedAccuracy
            if (currentSpeedAccuracy != null && location.speed < currentSpeedAccuracy * 0.68) {
                0f
            } else {
                location.speed
            }
        } else {
            0f
        }

        _bearing = if (location.hasBearing()) {
            location.bearing
        } else {
            null
        }

        _bearingAccuracy = if (LocationCompat.hasBearingAccuracy(location)) {
            LocationCompat.getBearingAccuracyDegrees(location)
        } else {
            null
        }

        if (notify) notifyListeners()
    }

    companion object {
        fun isAvailable(context: Context): Boolean {
            if (!Permissions.canGetLocation(context)) {
                return false
            }

            val lm = context.getSystemService<LocationManager>()
            return tryOrDefault(false) {
                return lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
            }
        }
    }
}
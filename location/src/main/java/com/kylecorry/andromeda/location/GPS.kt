package com.kylecorry.andromeda.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.getSystemService
import androidx.core.location.LocationCompat
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
    private val frequency: Duration = Duration.ofSeconds(20),
    private val minDistance: Distance = Distance.meters(0f)
) : AbstractSensor(),
    IGPS {

    override val hasValidReading: Boolean
        get() = location != Coordinate.zero

    override val satellites: Int
        get() = _satellites

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

    private var _altitude = 0f
    private var _time = Instant.now()
    private var _quality = Quality.Unknown
    private var _horizontalAccuracy: Float? = null
    private var _verticalAccuracy: Float? = null
    private var _satellites: Int = 0
    private var _speed: Float = 0f
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
    }

    override fun stopImpl() {
        locationManager?.removeUpdates(locationListener)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            nmeaListener?.let { locationManager?.removeNmeaListener(it) }
        } else {
            @Suppress("DEPRECATION")
            locationManager?.removeNmeaListener(legacyNmeaListener)
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
        _satellites =
            if (location.extras?.containsKey("satellites") == true) (location.extras?.getInt("satellites")
                ?: 0) else 0
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
        // TODO: Add speed accuracy to IGPS
        _speed = if (location.hasSpeed()) {
            if (LocationCompat.hasSpeedAccuracy(location) &&
                location.speed < LocationCompat.getSpeedAccuracyMetersPerSecond(location) * 0.68
            ) {
                0f
            } else {
                location.speed
            }
        } else {
            0f
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
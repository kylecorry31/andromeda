package com.kylecorry.andromeda.sense.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.getSystemService
import androidx.core.location.LocationCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.sensors.Quality
import com.kylecorry.andromeda.core.tryOrDefault
import com.kylecorry.andromeda.core.tryOrNothing
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.DistanceUnits
import com.kylecorry.sol.units.Speed
import com.kylecorry.sol.units.TimeUnits
import java.time.Instant

@SuppressLint("MissingPermission")
abstract class BaseGPS(
    protected val context: Context,
    private val provider: String,
    private val requestConfig: LocationRequestConfig = LocationRequestConfig()
) : AbstractSensor(),
    IGPS {

    override val hasValidReading: Boolean
        get() = location != Coordinate.zero

    override var quality: Quality = Quality.Unknown
        protected set

    override var horizontalAccuracy: Float? = null
        protected set

    override var verticalAccuracy: Float? = null
        protected set

    override var location: Coordinate = Coordinate.zero
        protected set

    override var speed: Speed = Speed.from(0f, DistanceUnits.Meters, TimeUnits.Seconds)
        protected set

    override var time: Instant = Instant.now()
        protected set

    override var altitude: Float = 0f
        protected set

    override var mslAltitude: Float? = null
        protected set

    override val bearing: Bearing?
        get() = rawBearing?.let { Bearing.from(it) }

    override var rawBearing: Float? = null
        protected set

    override var bearingAccuracy: Float? = null
        protected set

    override var speedAccuracy: Float? = null
        protected set
    override var fixTimeElapsedNanos: Long? = null
        protected set

    protected val locationManager by lazy { context.getSystemService<LocationManager>() }
    private val locationListener = SimpleLocationListener { updateLastLocation(it, true) }

    init {
        tryOrNothing {
            if (Permissions.canGetLocation(context)) {
                updateLastLocation(
                    locationManager?.getLastKnownLocation(provider),
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
            locationManager?.getLastKnownLocation(provider),
            false
        )

        val builder = LocationRequestCompat.Builder(requestConfig.frequency.toMillis())
            .setQuality(requestConfig.powerUsage.toLocationRequestQuality())
            .setMinUpdateDistanceMeters(requestConfig.minimumDistance.meters().value)

        if (requestConfig.minimumFrequency != null) {
            builder.setMinUpdateIntervalMillis(requestConfig.minimumFrequency.toMillis())
        }

        if (requestConfig.maximumUpdates != null) {
            builder.setMaxUpdates(requestConfig.maximumUpdates)
        }

        if (requestConfig.maximumUpdateDelay != null) {
            builder.setMaxUpdateDelayMillis(requestConfig.maximumUpdateDelay.toMillis())
        }

        if (requestConfig.maximumDuration != null) {
            builder.setDurationMillis(requestConfig.maximumDuration.toMillis())
        }

        val request = builder.build()

        locationManager?.let {
            LocationManagerCompat.requestLocationUpdates(
                it,
                provider,
                request,
                locationListener,
                Looper.getMainLooper()
            )
        }
    }

    override fun stopImpl() {
        locationManager?.let {
            LocationManagerCompat.removeUpdates(it, locationListener)
        }
    }

    protected open fun updateLastLocation(location: Location?, notify: Boolean = true) {
        if (location == null) {
            return
        }

        this.location = Coordinate(location.latitude, location.longitude)

        time = Instant.ofEpochMilli(location.time)

        fixTimeElapsedNanos = location.elapsedRealtimeNanos

        altitude = if (location.hasAltitude()) location.altitude.toFloat() else 0f

        val accuracy = if (location.hasAccuracy()) location.accuracy else null
        quality = when {
            accuracy != null && accuracy < ACCURACY_HIGH_THRESHOLD -> Quality.Good
            accuracy != null && accuracy < ACCURACY_MEDIUM_THRESHOLD -> Quality.Moderate
            accuracy != null -> Quality.Poor
            else -> Quality.Unknown
        }

        horizontalAccuracy = accuracy

        verticalAccuracy = if (LocationCompat.hasVerticalAccuracy(location)) {
            LocationCompat.getVerticalAccuracyMeters(location)
        } else {
            null
        }

        speedAccuracy = if (LocationCompat.hasSpeedAccuracy(location)) {
            LocationCompat.getSpeedAccuracyMetersPerSecond(location)
        } else {
            null
        }

        speed = Speed.from(
            if (location.hasSpeed()) location.speed else 0f,
            DistanceUnits.Meters,
            TimeUnits.Seconds
        )

        rawBearing = if (location.hasBearing()) {
            location.bearing
        } else {
            null
        }

        bearingAccuracy = if (LocationCompat.hasBearingAccuracy(location)) {
            LocationCompat.getBearingAccuracyDegrees(location)
        } else {
            null
        }

        if (notify) notifyListeners()
    }

    companion object {
        const val ACCURACY_HIGH_THRESHOLD = 8f
        const val ACCURACY_MEDIUM_THRESHOLD = 16f

        fun isAvailable(context: Context, provider: String): Boolean {
            if (!Permissions.canGetLocation(context)) {
                return false
            }

            val lm = context.getSystemService<LocationManager>()
            return tryOrDefault(false) {
                return lm?.isProviderEnabled(provider) ?: false
            }
        }
    }
}

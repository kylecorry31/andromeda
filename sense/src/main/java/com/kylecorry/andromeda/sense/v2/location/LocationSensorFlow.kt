package com.kylecorry.andromeda.sense.v2.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.location.LocationCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.andromeda.sense.location.LocationRequestConfig
import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.DistanceUnits
import com.kylecorry.sol.units.Speed
import com.kylecorry.sol.units.TimeUnits
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import java.time.Instant

@SuppressLint("MissingPermission")
class LocationSensorFlow(
    private val context: Context,
    private val provider: String,
    private val requestConfig: LocationRequestConfig = LocationRequestConfig()
) : SensorFlow<LocationSensorReading>() {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    init {
        if (Permissions.canGetLocation(context) && locationManager.isProviderEnabled(provider)) {
            setLastKnownValue(locationManager.getLastKnownLocation(provider)?.let(::map))
        }
    }

    override suspend fun ProducerScope<LocationSensorReading>.setupListener() {
        if (!Permissions.canGetLocation(context) || !locationManager.isProviderEnabled(provider)) {
            close()
            return
        }

        // Get the last known location, but don't notify as it may be stale
        setLastKnownValue(locationManager.getLastKnownLocation(provider)?.let(::map))

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
        val callback = LocationListenerCompat { location: Location ->
            trySend(map(location))
        }

        LocationManagerCompat.requestLocationUpdates(
            locationManager,
            provider,
            request,
            callback,
            Looper.getMainLooper()
        )
        setIsActive(true)

        awaitClose {
            LocationManagerCompat.removeUpdates(locationManager, callback)
            setIsActive(false)
        }
    }

    private fun map(location: Location): LocationSensorReading {
        return LocationSensorReading(
            eventTime = Instant.ofEpochMilli(location.time),
            eventTimeElapsedNanos = location.elapsedRealtimeNanos,
            location = Coordinate(location.latitude, location.longitude),
            horizontalAccuracy = if (location.hasAccuracy()) Distance.meters(location.accuracy) else null,
            altitude = if (location.hasAltitude()) Distance.meters(location.altitude.toFloat()) else null,
            mslAltitude = if (LocationCompat.hasMslAltitude(location)) Distance.meters(
                LocationCompat.getMslAltitudeMeters(
                    location
                ).toFloat()
            ) else null,
            mslAccuracy = if (LocationCompat.hasMslAltitudeAccuracy(location)) Distance.meters(
                LocationCompat.getMslAltitudeAccuracyMeters(
                    location
                )
            ) else null,
            verticalAccuracy = if (LocationCompat.hasVerticalAccuracy(location)) Distance.meters(
                LocationCompat.getVerticalAccuracyMeters(
                    location
                )
            ) else null,
            bearing = if (location.hasBearing()) Bearing.from(location.bearing) else null,
            bearingAccuracy = if (LocationCompat.hasBearingAccuracy(location)) LocationCompat.getBearingAccuracyDegrees(
                location
            ) else null,
            speed = if (location.hasSpeed()) Speed.from(
                location.speed,
                DistanceUnits.Meters,
                TimeUnits.Seconds
            ) else null,
            speedAccuracy = if (LocationCompat.hasSpeedAccuracy(location)) Speed.from(
                LocationCompat.getSpeedAccuracyMetersPerSecond(
                    location
                ), DistanceUnits.Meters, TimeUnits.Seconds
            ) else null
        )
    }
}

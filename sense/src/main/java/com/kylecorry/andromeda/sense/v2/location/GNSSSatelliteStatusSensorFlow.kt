package com.kylecorry.andromeda.sense.v2.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.getSystemService
import androidx.core.location.LocationManagerCompat
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.andromeda.sense.location.Satellite
import com.kylecorry.andromeda.sense.location.SimpleGnssStatusListener
import com.kylecorry.andromeda.sense.v2.SensorFlow
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import java.time.Instant

class GNSSSatelliteStatusSensorFlow(private val context: Context) :
    SensorFlow<SatelliteStatusReading>() {
    private val locationManager by lazy { context.getSystemService<LocationManager>() }

    @SuppressLint("MissingPermission")
    override suspend fun ProducerScope<SatelliteStatusReading>.setupListener() {
        if (!Permissions.canGetFineLocation(context)) {
            close()
            return
        }
        val manager = locationManager ?: run {
            close()
            return
        }
        val listener = SimpleGnssStatusListener { status ->
            val details = Satellite.fromStatus(status)
            trySend(
                SatelliteStatusReading(
                    eventTime = Instant.now(),
                    eventTimeElapsedNanos = SystemClock.elapsedRealtimeNanos(),
                    satellites = details.count { it.usedInFix },
                    satelliteDetails = details
                )
            )
        }
        if (!LocationManagerCompat.registerGnssStatusCallback(
                manager,
                listener,
                Handler(Looper.getMainLooper())
            )
        ) {
            close()
            return
        }
        setIsActive(true)
        awaitClose {
            LocationManagerCompat.unregisterGnssStatusCallback(manager, listener)
            setIsActive(false)
        }
    }
}

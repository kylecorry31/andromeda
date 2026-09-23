package com.kylecorry.andromeda.sense.v2.altitude

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.andromeda.sense.location.Nmea
import com.kylecorry.andromeda.sense.location.SimpleNmeaListener
import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.sol.units.Distance
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import java.time.Instant

class NMEAAltimeterSensorFlow(private val context: Context) : SensorFlow<NMEAAltimeterReading>() {
    private val locationManager by lazy { context.getSystemService<LocationManager>() }

    @SuppressLint("MissingPermission")
    override suspend fun ProducerScope<NMEAAltimeterReading>.setupListener() {
        if (!Permissions.canGetFineLocation(context)) {
            close()
            return
        }
        val listener = SimpleNmeaListener { message ->
            val altitude = Nmea(message).mslAltitude ?: return@SimpleNmeaListener
            trySend(
                NMEAAltimeterReading(
                    eventTime = Instant.now(),
                    eventTimeElapsedNanos = SystemClock.elapsedRealtimeNanos(),
                    altitude = Distance.meters(altitude)
                )
            )
        }
        val registered = locationManager?.addNmeaListener(listener, Handler(Looper.getMainLooper())) == true
        if (!registered) {
            close()
            return
        }
        setIsActive(true)
        awaitClose {
            locationManager?.removeNmeaListener(listener)
            setIsActive(false)
        }
    }
}

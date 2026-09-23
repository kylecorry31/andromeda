package com.kylecorry.andromeda.sense.v2.altitude

import android.os.SystemClock
import android.util.Log
import com.kylecorry.andromeda.core.math.DecimalFormatter
import com.kylecorry.andromeda.core.time.Throttle
import com.kylecorry.andromeda.preferences.IPreferences
import com.kylecorry.andromeda.sense.location.filters.GPSPassThroughAltitudeFilter
import com.kylecorry.andromeda.sense.location.filters.IGPSAltitudeFilter
import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.andromeda.sense.v2.barometer.BarometerReading
import com.kylecorry.andromeda.sense.v2.location.LocationSensorReading
import com.kylecorry.sol.math.interpolation.Interpolation
import com.kylecorry.sol.science.meteorology.Meteorology
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Pressure
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Duration
import java.time.Instant

class FusedAltimeterSensorFlow(
    private val gps: SensorFlow<LocationSensorReading>,
    private val barometer: SensorFlow<BarometerReading>,
    private val cache: IPreferences,
    private val useContinuousCalibration: Boolean = true,
    private val gpsFilter: IGPSAltitudeFilter = GPSPassThroughAltitudeFilter(),
    private val recalibrationInterval: Duration = Duration.ofHours(1),
    private val useMSLAltitude: Boolean = true,
    private val shouldLog: Boolean = false,
    private val maxGPSFixAge: Duration = Duration.ofSeconds(30),
    private val recalibrationTimeout: Duration = Duration.ofSeconds(30)
) : SensorFlow<FusedAltimeterReading>() {

    @Volatile
    private var latestBarometerReading: BarometerReading? = null
    private var latestGpsReading: LocationSensorReading? = null
    private var hasPendingGPSUpdate = false
    private val recalibrationThrottle = Throttle(RECALIBRATION_RETRY_INTERVAL.toMillis())

    override suspend fun ProducerScope<FusedAltimeterReading>.setupListener() {
        latestBarometerReading = barometer.lastKnown.value
        latestGpsReading = gps.lastKnown.value
        hasPendingGPSUpdate = false
        gpsFilter.reset()

        val gpsUpdates = Channel<LocationSensorReading>(
            if (useContinuousCalibration) Channel.UNLIMITED else Channel.CONFLATED
        )
        val barometerJob = launch {
            barometer.flow.collect { latestBarometerReading = it }
        }
        val gpsJob = if (useContinuousCalibration) {
            launch { gps.flow.collect { gpsUpdates.send(it) } }
        } else {
            null
        }
        val activeJob = launch {
            combine(gps.isActive, barometer.isActive) { gpsActive, barometerActive ->
                gpsActive || barometerActive
            }.collect(::setIsActive)
        }
        val updateJob = launch {
            while (isActive) {
                delay(UPDATE_FREQUENCY.toMillis())
                if (useContinuousCalibration) {
                    drainGPSUpdates(gpsUpdates)
                }
                val reading = update(gpsUpdates) ?: continue
                trySend(reading)
            }
        }

        awaitClose {
            barometerJob.cancel()
            gpsJob?.cancel()
            activeJob.cancel()
            updateJob.cancel()
            gpsUpdates.close()
            setIsActive(false)
        }
    }

    private fun drainGPSUpdates(gpsUpdates: Channel<LocationSensorReading>) {
        while (true) {
            val reading = gpsUpdates.tryReceive().getOrNull() ?: return
            onGPSUpdate(reading)
        }
    }

    private fun onGPSUpdate(reading: LocationSensorReading) {
        val altitude = getGPSAltitude(reading) ?: return
        gpsFilter.update(
            altitude,
            reading.altitudeAccuracy?.meters()?.value,
            reading.eventTimeElapsedNanos
        )
        latestGpsReading = reading
        hasPendingGPSUpdate = true
    }

    private suspend fun update(
        gpsUpdates: Channel<LocationSensorReading>
    ): FusedAltimeterReading? {
        val barometer = latestBarometerReading ?: return null
        val pressure = barometer.pressure.hpa()
        if (pressure.value == 0f) return null

        val seaLevelPressure = getLastSeaLevelPressure()
        if (seaLevelPressure == null) {
            recalibrate(gpsUpdates, timeout = null)
            return null
        }

        if (isSeaLevelPressureExpired() &&
            !recalibrationThrottle.isThrottled() &&
            recalibrate(gpsUpdates, recalibrationTimeout)
        ) {
            return null
        }

        val barometricAltitude = Meteorology.getAltitude(pressure, seaLevelPressure).value
        val gpsHasFix = hasGPSFix()
        val gpsAltitude = if (gpsHasFix) getGPSAltitude() else barometricAltitude
        val gpsError = if (gpsHasFix) gpsFilter.accuracy ?: MAX_GPS_ERROR else MAX_GPS_ERROR

        val gpsWeight = if (useContinuousCalibration && hasPendingGPSUpdate) {
            hasPendingGPSUpdate = false
            val errorWeight = 1 - Interpolation.map(
                gpsError,
                0f,
                MAX_GPS_ERROR,
                MIN_ALPHA,
                MAX_ALPHA
            ).coerceIn(MIN_ALPHA, MAX_ALPHA)
            val timeSinceLastGPSUsed = getTimeSinceLastGPSUsed()?.seconds?.toFloat() ?: 0f
            val timeWeight = Interpolation.map(
                timeSinceLastGPSUsed,
                0f,
                MAX_TIME_FOR_WEIGHT.seconds.toFloat(),
                0f,
                MAX_ALPHA_TIME
            ).coerceIn(0f, MAX_ALPHA_TIME)
            (errorWeight + timeWeight).coerceIn(0f, 1f)
        } else {
            0f
        }

        val altitude = gpsWeight * gpsAltitude + (1 - gpsWeight) * barometricAltitude
        setLastSeaLevelPressure(
            Meteorology.getSeaLevelPressure(pressure, Distance.meters(altitude)),
            isBaseline = false,
            wasGPSUsed = gpsWeight > 0
        )

        if (shouldLog) {
            Log.d(
                "FusedAltimeter",
                "ALT: ${DecimalFormatter.format(altitude, 2, true)}, " +
                    "GPS: ${DecimalFormatter.format(getGPSAltitude(), 2, true)}, " +
                    "BAR: ${DecimalFormatter.format(pressure.value, 2, true)}, " +
                    "SEA: ${DecimalFormatter.format(seaLevelPressure.value, 2, true)}, " +
                    "ALPHA: ${DecimalFormatter.format(gpsWeight, 3, true)}, " +
                    "ERR: ${DecimalFormatter.format(gpsError, 2, true)}"
            )
        }

        if (useContinuousCalibration && gpsWeight <= 0f) return null
        return FusedAltimeterReading(
            eventTime = barometer.eventTime,
            eventTimeElapsedNanos = barometer.eventTimeElapsedNanos,
            altitude = Distance.meters(altitude)
        )
    }

    private fun getGPSAltitude(reading: LocationSensorReading): Float? {
        val altitude = if (useMSLAltitude) reading.mslAltitude ?: reading.altitude else reading.altitude
        return altitude?.meters()?.value
    }

    private fun getGPSAltitude(): Float {
        val reading = latestGpsReading
        val fallbackAltitude = reading?.let(::getGPSAltitude) ?: 0f
        return if (gpsFilter.altitude == 0f && !hasFilteredGPSFix()) {
            fallbackAltitude
        } else {
            gpsFilter.altitude
        }
    }

    private fun hasGPSFix(): Boolean {
        val timestamp = latestGpsReading?.eventTimeElapsedNanos ?: return false
        val age = SystemClock.elapsedRealtimeNanos() - timestamp
        return age <= maxGPSFixAge.toNanos()
    }

    private fun hasFilteredGPSFix() = hasGPSFix() && gpsFilter.hasValidReading

    private suspend fun recalibrate(
        gpsUpdates: Channel<LocationSensorReading>,
        timeout: Duration?
    ): Boolean {
        val barometer = latestBarometerReading ?: return false
        val pressure = barometer.pressure.hpa()
        if (pressure.value <= 0f) return false

        gpsFilter.reset()
        while (gpsUpdates.tryReceive().isSuccess) Unit

        withTimeoutOrNull(timeout?.toMillis() ?: Long.MAX_VALUE) {
            if (useContinuousCalibration) {
                while (!gpsFilter.hasValidReading) {
                    val reading = gpsUpdates.receive()
                    if (getGPSAltitude(reading) != null) {
                        onGPSUpdate(reading)
                    }
                }
                latestGpsReading
            } else {
                gps.flow.first {
                    if (getGPSAltitude(it) != null) {
                        onGPSUpdate(it)
                    }
                    gpsFilter.hasValidReading
                }
            }
        }
            ?: return false

        val currentPressure = latestBarometerReading?.pressure?.hpa() ?: return false
        if (currentPressure.value <= 0f) return false

        setLastSeaLevelPressure(
            Meteorology.getSeaLevelPressure(currentPressure, Distance.meters(getGPSAltitude())),
            isBaseline = true,
            wasGPSUsed = true
        )
        return true
    }

    private fun getLastSeaLevelPressure(): Pressure? {
        return cache.getFloat(LAST_SEA_LEVEL_PRESSURE_KEY)?.let(Pressure::hpa)
    }

    private fun isSeaLevelPressureExpired(): Boolean {
        val time = cache.getInstant(LAST_SEA_LEVEL_PRESSURE_TIME_KEY) ?: return true
        val timeSinceReading = Duration.between(time, Instant.now())
        return timeSinceReading > recalibrationInterval || timeSinceReading.isNegative
    }

    private fun getTimeSinceLastGPSUsed(): Duration? {
        val time = cache.getInstant(LAST_GPS_USED_TIME_KEY) ?: return null
        return Duration.between(time, Instant.now())
    }

    private fun setLastSeaLevelPressure(
        pressure: Pressure,
        isBaseline: Boolean,
        wasGPSUsed: Boolean
    ) {
        cache.putFloat(LAST_SEA_LEVEL_PRESSURE_KEY, pressure.value)
        if (isBaseline) {
            cache.putInstant(LAST_SEA_LEVEL_PRESSURE_TIME_KEY, Instant.now())
        }
        if (wasGPSUsed) {
            cache.putInstant(LAST_GPS_USED_TIME_KEY, Instant.now())
        }
    }

    companion object {
        private const val LAST_SEA_LEVEL_PRESSURE_KEY =
            "andromeda_fused_altimeter_last_sea_level_pressure"
        private const val LAST_SEA_LEVEL_PRESSURE_TIME_KEY =
            "andromeda_fused_altimeter_last_sea_level_pressure_time"
        private const val LAST_GPS_USED_TIME_KEY = "andromeda_fused_altimeter_last_gps_used_time"

        private const val MIN_ALPHA = 0.9f
        private const val MAX_ALPHA = 0.99f
        private const val MAX_ALPHA_TIME = 0.4f
        private const val MAX_GPS_ERROR = 5f
        private val MAX_TIME_FOR_WEIGHT = Duration.ofHours(2)
        private val UPDATE_FREQUENCY = Duration.ofMillis(200)
        private val RECALIBRATION_RETRY_INTERVAL = Duration.ofMinutes(5)

        fun clearCachedCalibration(cache: IPreferences) {
            cache.remove(LAST_SEA_LEVEL_PRESSURE_KEY)
            cache.remove(LAST_SEA_LEVEL_PRESSURE_TIME_KEY)
            cache.remove(LAST_GPS_USED_TIME_KEY)
        }
    }
}

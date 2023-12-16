package com.kylecorry.andromeda.sense.altitude

import android.util.Log
import com.kylecorry.andromeda.core.coroutines.onDefault
import com.kylecorry.andromeda.core.math.DecimalFormatter
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.sensors.IAltimeter
import com.kylecorry.andromeda.core.time.CoroutineTimer
import com.kylecorry.andromeda.preferences.IPreferences
import com.kylecorry.andromeda.sense.barometer.IBarometer
import com.kylecorry.andromeda.sense.location.IGPS
import com.kylecorry.andromeda.sense.location.filters.GPSPassThroughAltitudeFilter
import com.kylecorry.andromeda.sense.location.filters.IGPSAltitudeFilter
import com.kylecorry.andromeda.sense.location.hasFix
import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.science.geology.Geology
import com.kylecorry.sol.science.meteorology.Meteorology
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Pressure
import java.time.Duration
import java.time.Instant

/**
 * A sensor that uses the GPS and barometer to calculate altitude.
 * @param gps The GPS sensor
 * @param barometer The barometer sensor
 * @param cache The cache to store calibration data
 * @param useContinuousCalibration True if the sensor should continuously calibrate using the GPS, otherwise it will only calibrate once an hour
 * @param gpsFilter The filter to use for the GPS altitude
 * @param recalibrationInterval The interval to recalibrate the sensor
 * @param useMSLAltitude True if the sensor should use the MSL altitude from the GPS, otherwise it will use the GPS altitude
 * @param shouldLog True if the sensor should log to logcat for debugging purposes
 */
class FusedAltimeter(
    private val gps: IGPS,
    private val barometer: IBarometer,
    private val cache: IPreferences,
    private val useContinuousCalibration: Boolean = true,
    private val gpsFilter: IGPSAltitudeFilter = GPSPassThroughAltitudeFilter(),
    private val recalibrationInterval: Duration = Duration.ofHours(1),
    private val useMSLAltitude: Boolean = true,
    private val shouldLog: Boolean = false
) : AbstractSensor(), IAltimeter {

    private val updateTimer = CoroutineTimer {
        val success = onDefault {
            update()
        }
        if (success) {
            notifyListeners()
        }
    }

    private var fusedAltitude: Float? = null

    override val altitude: Float
        get() = fusedAltitude ?: getGPSAltitude()

    override val hasValidReading: Boolean
        get() = fusedAltitude != null

    private var hasPendingGPSUpdate = false

    override fun startImpl() {
        fusedAltitude = null
        hasPendingGPSUpdate = false
        gpsFilter.reset()
        if (useContinuousCalibration) {
            gps.start(this::onGPSUpdate)
        }
        barometer.start(this::onBarometerUpdate)
        updateTimer.interval(UPDATE_FREQUENCY)
    }

    override fun stopImpl() {
        updateTimer.stop()
        gps.stop(this::onGPSUpdate)
        barometer.stop(this::onBarometerUpdate)
    }

    private fun onBarometerUpdate(): Boolean {
        return true
    }

    private fun onGPSUpdate(): Boolean {
        gpsFilter.update(
            if (useMSLAltitude) gps.mslAltitude ?: gps.altitude else gps.altitude,
            gps.verticalAccuracy
        )
        hasPendingGPSUpdate = true
        return true
    }

    private suspend fun update(): Boolean {
        if (!barometer.hasValidReading || barometer.pressure == 0f) {
            // No barometer reading yet
            return false
        }

        val seaLevel = getLastSeaLevelPressure()

        // Sea level pressure is unknown, it needs to be calibrated
        if (seaLevel == null) {
            recalibrate()
            return false
        }

        // Calculate the barometric altitude
        val barometricAltitude =
            Geology.getAltitude(Pressure.hpa(barometer.pressure), seaLevel).distance

        // At this point, the barometric altitude is available but the GPS altitude may not be
        // If the GPS has a reading, use the GPS altimeter (even if the gaussian filter hasn't converged)
        // Otherwise, use the barometric altitude
        val gpsAltitude = if (hasGPSFix()) {
            getGPSAltitude()
        } else {
            barometricAltitude
        }

        // Dynamically calculate the influence of the GPS using the altitude accuracy
        // A more accurate GPS reading will have a higher weight
        // If always on calibration is disabled, the GPS will not be used
        val gpsError = if (hasGPSFix()) {
            gpsFilter.accuracy ?: MAX_GPS_ERROR
        } else {
            MAX_GPS_ERROR
        }
        val gpsWeight = if (useContinuousCalibration && hasPendingGPSUpdate) {
            hasPendingGPSUpdate = false

            // The weight is higher for more accurate GPS readings
            val errorWeight = 1 - SolMath.map(gpsError, 0f, MAX_GPS_ERROR, MIN_ALPHA, MAX_ALPHA)
                .coerceIn(MIN_ALPHA, MAX_ALPHA)

            // The weight increases over time (up to a maximum at MAX_TIME_FOR_WEIGHT hours)
            val timeSinceLastGPSUsed = getTimeSinceLastGPSUsed()?.seconds?.toFloat() ?: 0f
            val timeWeight = SolMath.map(
                timeSinceLastGPSUsed,
                0f,
                MAX_TIME_FOR_WEIGHT.seconds.toFloat(),
                0f,
                MAX_ALPHA_TIME
            ).coerceIn(0f, MAX_ALPHA_TIME)

            // Simply add the weights together and restrict to the range
            (errorWeight + timeWeight).coerceIn(0f, 1f)
        } else {
            0f
        }

        // Create the filter if it doesn't exist, the value will be overwritten so it doesn't matter
        val altitude = gpsWeight * gpsAltitude + (1 - gpsWeight) * barometricAltitude
        fusedAltitude = altitude

        // Update the current estimate of the sea level pressure
        setLastSeaLevelPressure(
            Meteorology.getSeaLevelPressure(
                Pressure.hpa(barometer.pressure),
                Distance.meters(altitude)
            ),
            isBaseline = false,
            wasGPSUsed = gpsWeight > 0
        )

        if (shouldLog) {
            Log.d(
                "FusedAltimeter",
                "ALT: ${DecimalFormatter.format(altitude, 2, true)}, " +
                        "GPS: ${DecimalFormatter.format(getGPSAltitude(), 2, true)}, " +
                        "BAR: ${DecimalFormatter.format(barometer.pressure, 2, true)}, " +
                        "SEA: ${DecimalFormatter.format(seaLevel.pressure, 2, true)}, " +
                        "ALPHA: ${DecimalFormatter.format(gpsWeight, 3, true)}, " +
                        "ERR: ${DecimalFormatter.format(gpsError, 2, true)}"
            )
        }

        // When continuous calibration is enabled, ensure a GPS fix before notifying listeners
        return if (useContinuousCalibration) {
            gpsWeight > 0
        } else {
            true
        }
    }

    private fun getGPSAltitude(): Float {
        val altitude = gpsFilter.altitude
        return if (altitude == 0f && !hasFilteredGPSFix()) {
            if (useMSLAltitude) gps.mslAltitude ?: gps.altitude else gps.altitude
        } else {
            altitude
        }
    }

    private suspend fun recalibrate() {
        if (barometer.pressure > 0f) {
            gpsFilter.reset()
            gps.read {
                onGPSUpdate()
                gpsFilter.hasValidReading
            }
            setLastSeaLevelPressure(
                Meteorology.getSeaLevelPressure(
                    Pressure.hpa(barometer.pressure),
                    Distance.meters(getGPSAltitude())
                ),
                isBaseline = true,
                wasGPSUsed = true
            )
        }
    }

    private fun hasGPSFix(): Boolean {
        return gps.hasFix()
    }

    private fun hasFilteredGPSFix(): Boolean {
        return hasGPSFix() && gpsFilter.hasValidReading
    }

    private fun getLastSeaLevelPressure(): Pressure? {
        val time = cache.getInstant(LAST_SEA_LEVEL_PRESSURE_TIME_KEY) ?: return null

        val timeSinceReading = Duration.between(time, Instant.now())
        if (timeSinceReading > recalibrationInterval || timeSinceReading.isNegative) {
            // Sea level pressure has expired
            return null
        }

        return cache.getFloat(LAST_SEA_LEVEL_PRESSURE_KEY)?.let { Pressure.hpa(it) }
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
        cache.putFloat(LAST_SEA_LEVEL_PRESSURE_KEY, pressure.pressure)

        // Only update the time if the gaussian GPS was used - this prevents only using the barometer over the long term
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
        private val MAX_TIME_FOR_WEIGHT = Duration.ofHours(2)
        private const val MAX_GPS_ERROR = 5f
        private val UPDATE_FREQUENCY = Duration.ofMillis(200)

        /**
         * Clear the cached calibration data
         * @param cache The cache to clear from
         */
        fun clearCachedCalibration(cache: IPreferences) {
            cache.remove(LAST_SEA_LEVEL_PRESSURE_KEY)
            cache.remove(LAST_SEA_LEVEL_PRESSURE_TIME_KEY)
            cache.remove(LAST_GPS_USED_TIME_KEY)
        }
    }
}
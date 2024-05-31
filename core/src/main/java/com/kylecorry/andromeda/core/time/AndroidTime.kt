package com.kylecorry.andromeda.core.time

import android.os.SystemClock
import java.time.Duration
import java.time.Instant

object AndroidTime {

    /**
     * Gets the time in milliseconds since boot (excluding time spent in deep sleep)
     * @return the uptime in milliseconds
     */
    fun uptimeMillis(): Long {
        return SystemClock.uptimeMillis()
    }

    /**
     * Gets the time since boot (excluding time spent in deep sleep)
     * @return the uptime
     */
    fun uptime(): Duration {
        return Duration.ofMillis(uptimeMillis())
    }

    /**
     * Gets the time in milliseconds since boot (including time spent in deep sleep)
     * @return the time since boot
     */
    fun millisSinceBoot(): Long {
        return SystemClock.elapsedRealtime()
    }

    /**
     * Gets the time since boot (including time spent in deep sleep)
     * @return the time since boot
     */
    fun timeSinceBoot(): Duration {
        return Duration.ofMillis(millisSinceBoot())
    }

    /**
     * Converts a time in milliseconds since boot to Unix time (milliseconds since epoch)
     * @param millisSinceBoot the time since boot
     * @return the Unix time in milliseconds
     */
    fun millisSinceBootToTime(millisSinceBoot: Long): Long {
        val currentTimeSinceBoot = millisSinceBoot()
        val delta = currentTimeSinceBoot - millisSinceBoot
        return System.currentTimeMillis() - delta
    }

    /**
     * Converts a time in milliseconds since boot to an Instant
     * @param millisSinceBoot the time since boot
     * @return the Instant
     */
    fun millisSinceBootToInstant(millisSinceBoot: Long): Instant {
        return Instant.ofEpochMilli(millisSinceBootToTime(millisSinceBoot))
    }

}
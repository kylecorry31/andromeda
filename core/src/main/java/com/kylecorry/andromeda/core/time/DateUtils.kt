package com.kylecorry.andromeda.core.time

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

internal object DateUtils {
    fun getClosestPastTime(
        currentTime: LocalDateTime,
        times: List<LocalDateTime?>
    ): LocalDateTime? {
        return times.filterNotNull().filter { it.isBefore(currentTime) }
            .minByOrNull { Duration.between(it, currentTime).abs() }
    }

    fun getClosestFutureTime(
        currentTime: LocalDateTime,
        times: List<LocalDateTime?>
    ): LocalDateTime? {
        return times.filterNotNull().filter { it.isAfter(currentTime) }
            .minByOrNull { Duration.between(it, currentTime).abs() }
    }

    fun getClosestTime(
        currentTime: ZonedDateTime,
        times: List<ZonedDateTime?>
    ): ZonedDateTime? {
        return times.filterNotNull().minByOrNull { Duration.between(it, currentTime).abs() }
    }

    fun getClosestFutureTime(
        currentTime: ZonedDateTime,
        times: List<ZonedDateTime?>
    ): ZonedDateTime? {
        return times.filterNotNull().filter { it.isAfter(currentTime) }
            .minByOrNull { Duration.between(it, currentTime).abs() }
    }
}
package com.kylecorry.andromeda.core.time

import java.time.*

fun LocalDateTime.toZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.of(this, ZoneId.systemDefault())
}

fun LocalDateTime.toEpochMillis(): Long {
    return this.toZonedDateTime().toEpochSecond() * 1000
}

fun ZonedDateTime.toUTCLocal(): LocalDateTime {
    return LocalDateTime.ofInstant(this.toInstant(), ZoneId.of("UTC"))
}

fun ZonedDateTime.atStartOfDay(): ZonedDateTime {
    return ZonedDateTime.of(this.toLocalDate(), LocalTime.MIN, this.zone)
}

fun ZonedDateTime.atEndOfDay(): ZonedDateTime {
    return ZonedDateTime.of(this.toLocalDate(), LocalTime.MAX, this.zone)
}

fun LocalDateTime.roundNearestMinute(minutes: Long): LocalDateTime {
    val minute = this.minute
    val newMinute = (minute / minutes) * minutes

    val diff = newMinute - minute
    return this.plusMinutes(diff)
}

fun Instant.toZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.ofInstant(this, ZoneId.systemDefault())
}

fun LocalDateTime.plusHours(hours: Double): LocalDateTime {
    val h = hours.toLong()
    val m = (hours % 1) * 60
    val s = (m % 1) * 60
    val ns = (1e9 * s).toLong()
    return this.plusHours(h).plusMinutes(m.toLong()).plusNanos(ns)
}

fun duration(hours: Long = 0L, minutes: Long = 0L, seconds: Long = 0L): Duration {
    return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds)
}

fun hours(hours: Float): Duration {
    val h = hours.toLong()
    val m = ((hours * 60) % 60).toLong()
    val s = ((hours * 3600) % 3600).toLong()
    return Duration.ofHours(h).plusMinutes(m).plusSeconds(s)
}

fun Instant.isInPast(): Boolean {
    return this < Instant.now()
}

fun Instant.isOlderThan(duration: Duration): Boolean {
    return Duration.between(this, Instant.now()) > duration
}
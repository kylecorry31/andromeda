package com.kylecorry.andromeda.sense.v2.readings

import java.time.Instant

interface EventTimeReading {
    val eventTime: Instant
    val eventTimeElapsedNanos: Long
}

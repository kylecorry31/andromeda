package com.kylecorry.andromeda.sense.v2

import java.time.Instant

interface ISensorReading {
    val eventTime: Instant
    val eventTimeElapsedNanos: Long
}

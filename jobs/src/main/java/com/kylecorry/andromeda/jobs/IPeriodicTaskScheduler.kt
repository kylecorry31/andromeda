package com.kylecorry.andromeda.jobs

import java.time.Duration
import java.time.Instant

interface IPeriodicTaskScheduler {
    fun interval(period: Duration, initialDelay: Duration = Duration.ZERO)
    fun interval(period: Duration, start: Instant)
    fun cancel()
}
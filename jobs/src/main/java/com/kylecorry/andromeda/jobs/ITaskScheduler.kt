package com.kylecorry.andromeda.jobs

import java.time.Duration
import java.time.Instant

interface ITaskScheduler {

    fun schedule(delay: Duration)
    fun schedule(time: Instant)

    fun cancel()

}
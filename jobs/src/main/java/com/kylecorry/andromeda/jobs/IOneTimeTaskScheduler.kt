package com.kylecorry.andromeda.jobs

import java.time.Duration
import java.time.Instant

interface IOneTimeTaskScheduler: ITaskScheduler {
    fun once(delay: Duration = Duration.ZERO)
    fun once(time: Instant)
    fun start()
}
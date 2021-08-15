package com.kylecorry.andromeda.jobs

import android.app.PendingIntent
import android.content.Context
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

class ExactTaskScheduler(
    private val context: Context,
    private val task: () -> PendingIntent
) :
    ITaskScheduler {


    override fun schedule(delay: Duration) {
        Alarms.set(
            context,
            LocalDateTime.now().plus(delay),
            task(),
            exact = true,
            allowWhileIdle = true
        )
    }

    override fun schedule(time: Instant) {
        schedule(Duration.between(Instant.now(), time))
    }

    override fun cancel() {
        Alarms.cancel(context, task())
    }
}
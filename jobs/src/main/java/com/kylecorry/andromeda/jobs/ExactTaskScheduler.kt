package com.kylecorry.andromeda.jobs

import android.app.PendingIntent
import android.content.Context
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

@Deprecated(
    message = "Use AlarmTaskScheduler instead", replaceWith = ReplaceWith(
        expression = "AlarmTaskScheduler(context, task)",
        imports = ["com.kylecorry.andromeda.jobs.AlarmTaskScheduler"]
    )
)
class ExactTaskScheduler(
    private val context: Context,
    private val task: () -> PendingIntent
) :
    IOneTimeTaskScheduler {


    override fun once(delay: Duration) {
        Alarms.set(
            context,
            LocalDateTime.now().plus(delay),
            task(),
            exact = true,
            allowWhileIdle = true
        )
    }

    override fun once(time: Instant) {
        once(Duration.between(Instant.now(), time))
    }

    override fun start() {
        once()
    }

    override fun cancel() {
        Alarms.cancel(context, task())
    }
}
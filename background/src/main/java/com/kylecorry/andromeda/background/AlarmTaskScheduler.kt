package com.kylecorry.andromeda.background

import android.app.PendingIntent
import android.content.Context
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

/**
 * Uses the AlarmManager to scheduler work. Useful for work that needs to run at an exact time.
 * @param context the context
 * @param exact determines if this should be run at exactly the time specified. Defaults to true. Deferrable tasks should use the WorkTaskScheduler
 * @param allowWhileIdle determines if this should be run while the device is idle. Defaults to false.
 * @param task the task to run - if you are just calling a broadcast receiver, it is recommend to use the AlarmBroadcastTaskScheduler
 */
class AlarmTaskScheduler(
    private val context: Context,
    private val exact: Boolean = true,
    private val allowWhileIdle: Boolean = false,
    private val task: () -> PendingIntent
) :
    IOneTimeTaskScheduler {


    override fun once(delay: Duration) {
        Alarms.set(
            context,
            LocalDateTime.now().plus(delay),
            task(),
            exact,
            allowWhileIdle
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
package com.kylecorry.andromeda.jobs

import android.app.PendingIntent
import android.content.Context
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

/**
 * Schedules an alarm clock
 * @param context the context
 * @param viewTask the task to run when the user wants to view the alarm
 * @param task the task to run
 */
class AlarmClockTaskScheduler(
    private val context: Context,
    private val viewTask: PendingIntent,
    private val task: () -> PendingIntent
) :
    ITaskScheduler {


    override fun schedule(delay: Duration) {
        Alarms.setAlarmClock(
            context,
            LocalDateTime.now().plus(delay),
            task(),
            viewTask
        )
    }

    override fun schedule(time: Instant) {
        schedule(Duration.between(Instant.now(), time))
    }

    override fun cancel() {
        Alarms.cancel(context, task())
    }
}
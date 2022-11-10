package com.kylecorry.andromeda.jobs

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

/**
 * Uses the AlarmManager and Broadcast Receivers to scheduler work. Useful for work that needs to run at an exact time.
 * @param context the context
 * @param receiver the receiver to call
 * @param uniqueId an ID used to identify the receiver
 * @param exact determines if this should be run at exactly the time specified. Defaults to true. Deferrable tasks should use the WorkTaskScheduler
 * @param allowWhileIdle determines if this should be run while the device is idle. Defaults to false.
 * @param intentExtras extras to add to the intent
 */
class AlarmBroadcastTaskScheduler(
    private val context: Context,
    private val receiver: Class<out BroadcastReceiver>,
    private val uniqueId: Int,
    private val exact: Boolean = true,
    private val allowWhileIdle: Boolean = false,
    private val intentExtras: Bundle? = null
) :
    IOneTimeTaskScheduler, IPeriodicTaskScheduler {


    override fun once(delay: Duration) {
        cancel()
        Alarms.set(
            context,
            LocalDateTime.now().plus(delay),
            createPendingIntent(),
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

    override fun interval(period: Duration, initialDelay: Duration) {
        cancel()
        Alarms.setRepeating(
            context,
            LocalDateTime.now().plus(initialDelay),
            period,
            createPendingIntent()
        )
    }

    override fun interval(period: Duration, start: Instant) {
        interval(period, Duration.between(Instant.now(), start))
    }

    override fun cancel() {
        Alarms.cancel(context, createPendingIntent())
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(context, receiver)
        if (intentExtras != null) {
            intent.putExtras(intentExtras)
        }

        return PendingIntent.getBroadcast(
            context,
            uniqueId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
    }
}
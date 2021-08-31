package com.kylecorry.andromeda.jobs

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

class AlarmBroadcastTaskScheduler(
    private val context: Context,
    private val receiver: Class<out BroadcastReceiver>,
    private val uniqueId: Int,
    private val exact: Boolean = true,
    private val allowWhileIdle: Boolean = false
) :
    ITaskScheduler {


    override fun schedule(delay: Duration) {
        Alarms.set(
            context,
            LocalDateTime.now().plus(delay),
            createPendingIntent(),
            exact,
            allowWhileIdle
        )
    }

    override fun schedule(time: Instant) {
        schedule(Duration.between(Instant.now(), time))
    }

    override fun cancel() {
        Alarms.cancel(context, createPendingIntent())
    }

    private fun createPendingIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            uniqueId,
            Intent(context, receiver),
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
    }
}
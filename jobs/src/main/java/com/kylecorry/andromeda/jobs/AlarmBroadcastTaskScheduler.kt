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

class AlarmBroadcastTaskScheduler(
    private val context: Context,
    private val receiver: Class<out BroadcastReceiver>,
    private val uniqueId: Int,
    private val exact: Boolean = true,
    private val allowWhileIdle: Boolean = false,
    private val intentExtras: Bundle? = null
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
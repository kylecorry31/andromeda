package com.kylecorry.andromeda.background

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.work.ListenableWorker
import com.kylecorry.andromeda.core.annotations.ExperimentalUsage

@ExperimentalUsage("This class may change significantly to accommodate generic tasks")
class OneTimeTaskSchedulerFactory(private val context: Context) {

    fun deferrable(
        task: Class<out ListenableWorker>,
        uniqueId: Int,
        data: Bundle? = null
    ): IOneTimeTaskScheduler {
        return WorkTaskScheduler(
            context.applicationContext,
            task,
            WorkTaskScheduler.createStringId(context, uniqueId),
            input = data
        )
    }

    fun exact(
        task: Class<out BroadcastReceiver>,
        uniqueId: Int,
        data: Bundle? = null
    ): IOneTimeTaskScheduler {
        return AlarmBroadcastTaskScheduler(
            context.applicationContext,
            task,
            uniqueId,
            exact = true,
            allowWhileIdle = true,
            intentExtras = data
        )
    }

    fun alarm(
        task: Class<out BroadcastReceiver>,
        viewTask: PendingIntent,
        uniqueId: Int,
        data: Bundle? = null
    ): IOneTimeTaskScheduler {
        return AlarmClockTaskScheduler(
            context.applicationContext,
            viewTask
        ) { createAlarmPendingIntent(task, uniqueId, data) }
    }

    private fun createAlarmPendingIntent(
        receiver: Class<out BroadcastReceiver>,
        uniqueId: Int,
        data: Bundle?
    ): PendingIntent {
        val intent = Intent(context, receiver)
        if (data != null) {
            intent.putExtras(data)
        }

        return PendingIntent.getBroadcast(
            context,
            uniqueId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
    }

}
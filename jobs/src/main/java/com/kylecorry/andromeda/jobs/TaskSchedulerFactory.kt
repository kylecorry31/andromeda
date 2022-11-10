package com.kylecorry.andromeda.jobs

import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import androidx.work.ListenableWorker
import com.kylecorry.andromeda.core.annotations.ExperimentalUsage

@ExperimentalUsage("This class may change significantly to accommodate generic tasks")
class TaskSchedulerFactory(private val context: Context) {

    @Suppress("UNCHECKED_CAST")
    fun once(
        task: Class<*>,
        uniqueId: Int,
        data: Bundle? = null
    ): IOneTimeTaskScheduler {
        if (ListenableWorker::class.java.isAssignableFrom(task)) {
            return WorkTaskScheduler(
                context.applicationContext,
                task as Class<out ListenableWorker>,
                WorkTaskScheduler.createStringId(context, uniqueId),
                input = data
            )
        }

        if (BroadcastReceiver::class.java.isAssignableFrom(task)) {
            return AlarmBroadcastTaskScheduler(
                context.applicationContext,
                task as Class<out BroadcastReceiver>,
                uniqueId,
                exact = true,
                allowWhileIdle = true,
                intentExtras = data
            )
        }

        throw IllegalArgumentException("The task must either be a BroadcastReceiver or ListenableWorker")
    }

    @Suppress("UNCHECKED_CAST")
    fun interval(
        task: Class<*>,
        uniqueId: Int,
        data: Bundle? = null
    ): IPeriodicTaskScheduler {
        if (ListenableWorker::class.java.isAssignableFrom(task)) {
            return WorkTaskScheduler(
                context.applicationContext,
                task as Class<out ListenableWorker>,
                WorkTaskScheduler.createStringId(context, uniqueId),
                input = data
            )
        }

        if (BroadcastReceiver::class.java.isAssignableFrom(task)) {
            return AlarmBroadcastTaskScheduler(
                context.applicationContext,
                task as Class<out BroadcastReceiver>,
                uniqueId,
                exact = true,
                allowWhileIdle = true,
                intentExtras = data
            )
        }

        throw IllegalArgumentException("The task must either be a BroadcastReceiver or ListenableWorker")
    }

}
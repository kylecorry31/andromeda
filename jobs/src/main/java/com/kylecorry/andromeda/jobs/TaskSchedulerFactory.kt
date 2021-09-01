package com.kylecorry.andromeda.jobs

import android.content.BroadcastReceiver
import android.content.Context
import androidx.work.ListenableWorker
import com.kylecorry.andromeda.core.annotations.ExperimentalUsage
import com.kylecorry.andromeda.core.system.Package

@ExperimentalUsage("This class may change significantly to accommodate generic tasks")
class TaskSchedulerFactory(private val context: Context) {

    fun deferrable(
        task: Class<out ListenableWorker>,
        uniqueId: Int
    ): ITaskScheduler {
        return WorkTaskScheduler(
            context.applicationContext,
            task,
            WorkTaskScheduler.createStringId(context, uniqueId)
        )
    }

    fun exact(
        task: Class<out BroadcastReceiver>,
        uniqueId: Int
    ): ITaskScheduler {
        return AlarmBroadcastTaskScheduler(
            context.applicationContext,
            task,
            uniqueId,
            exact = true,
            allowWhileIdle = true
        )
    }

}
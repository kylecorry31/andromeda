package com.kylecorry.andromeda.jobs

import android.content.Context
import androidx.work.*
import com.kylecorry.andromeda.core.system.Package
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

// TODO: Add support for expedited jobs

/**
 * This class uses the WorkManager for deferrable tasks
 * @param context the context
 * @param task the task to run
 * @param uniqueId a unique ID describing this task
 * @param expedited determines if this should be expedited (not yet implemented)
 * @param constraints the constraints around the work
 */
class WorkTaskScheduler(
    private val context: Context,
    private val task: Class<out ListenableWorker>,
    private val uniqueId: String,
    private val expedited: Boolean = false,
    private val constraints: Constraints? = null
) : ITaskScheduler {


    override fun schedule(delay: Duration) {
        val workManager = WorkManager.getInstance(context.applicationContext)

        val request = OneTimeWorkRequest
            .Builder(task).apply {
                addTag(uniqueId)
                setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
                if (constraints != null) {
                    setConstraints(constraints)
                }
            }
            .build()

        workManager.enqueueUniqueWork(
            uniqueId,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override fun schedule(time: Instant) {
        schedule(Duration.between(Instant.now(), time))
    }

    override fun cancel() {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(uniqueId)
    }

    companion object {
        fun createStringId(context: Context, uniqueId: Int): String {
            return Package.getPackageName(context) + "." + uniqueId
        }
    }
}
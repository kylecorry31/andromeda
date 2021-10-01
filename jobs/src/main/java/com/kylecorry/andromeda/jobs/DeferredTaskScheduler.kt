package com.kylecorry.andromeda.jobs

import android.content.Context
import androidx.work.*
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

@Deprecated(
    message = "Use WorkTaskScheduler instead", replaceWith = ReplaceWith(
        expression = "WorkTaskScheduler(context, task, uniqueId, false, constraints)",
        imports = ["com.kylecorry.andromeda.jobs.WorkTaskScheduler"]
    )
)
class DeferredTaskScheduler(
    private val context: Context,
    private val task: Class<out ListenableWorker>,
    private val uniqueId: String,
    private val constraints: Constraints? = null
) : ITaskScheduler {


    override fun once(delay: Duration) {
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

    override fun once(time: Instant) {
        once(Duration.between(Instant.now(), time))
    }

    override fun cancel() {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(uniqueId)
    }
}
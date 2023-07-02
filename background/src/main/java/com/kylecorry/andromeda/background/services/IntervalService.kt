package com.kylecorry.andromeda.background.services

import android.content.Intent
import android.content.IntentFilter
import androidx.core.os.bundleOf
import com.kylecorry.andromeda.background.BroadcastWorker
import com.kylecorry.andromeda.background.TaskSchedulerFactory
import com.kylecorry.andromeda.core.system.BroadcastReceiverTopic
import com.kylecorry.andromeda.core.time.Timer
import java.time.Duration

/**
 * A base service for running a background task on an interval.
 * This is only recommended for foreground services, since background services may be killed by the OS more often.
 * @param alwaysOnThreshold Determines when to switch from always on mode (constant wakelock) to scheduled jobs. Always on mode will be more accurate under 15 minutes, the scheduled jobs are inexact.
 * @param wakelockDuration The wakelock duration when running in deferred mode
 */
abstract class IntervalService(
    private val alwaysOnThreshold: Duration = Duration.ofMinutes(15),
    private val wakelockDuration: Duration? = null
) : AndromedaService() {
    abstract val period: Duration

    private val receiver by lazy {
        BroadcastReceiverTopic(this, IntentFilter.create(action, "text/plain"))
    }

    private val job by lazy {
        TaskSchedulerFactory(this).interval(
            BroadcastWorker::class.java,
            uniqueId,
            bundleOf("action" to action)
        )
    }

    private var isWakelockManaged = false

    private val timer = Timer {
        try {
            if (isWakelockManaged) {
                acquireWakelock(tag, wakelockDuration)
            }
            doWork()
        } finally {
            if (isWakelockManaged) {
                releaseWakelock()
            }
        }
    }

    abstract suspend fun doWork()

    open val action: String
        get() = "$tag.INTERVAL_ACTION"

    abstract val uniqueId: Int

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (period < alwaysOnThreshold) {
            acquireWakelock(tag)
            isWakelockManaged = false
            timer.interval(period)
        } else {
            isWakelockManaged = true
            receiver.subscribe(this::onReceive)
            job.interval(period)
        }
        return START_STICKY_COMPATIBILITY
    }

    override fun onDestroy() {
        releaseWakelock()
        receiver.unsubscribe(this::onReceive)
        timer.stop()
        job.cancel()
        super.onDestroy()
    }

    private fun onReceive(intent: Intent): Boolean {
        timer.once(Duration.ZERO)
        return true
    }
}
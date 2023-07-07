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
 * @param useOneTimeWorkers Use one time workers instead of a periodic worker when over the always on threshold. Using one time workers can lead to slightly more on time intervals and it also allows variable durations.
 */
abstract class IntervalService(
    private val alwaysOnThreshold: Duration = Duration.ofMinutes(15),
    private val wakelockDuration: Duration? = null,
    private val useOneTimeWorkers: Boolean = false
) : AndromedaService() {
    abstract val period: Duration

    private val receiver by lazy {
        BroadcastReceiverTopic(this, IntentFilter.create(action, "text/plain"))
    }

    private val periodicWorker by lazy {
        TaskSchedulerFactory(this).interval(
            BroadcastWorker::class.java,
            uniqueId,
            bundleOf("action" to action)
        )
    }

    private val oneTimeWorker by lazy {
        TaskSchedulerFactory(this).once(
            BroadcastWorker::class.java,
            uniqueId,
            bundleOf("action" to action)
        )
    }

    private var isWakelockManaged = false
    private var isEnabled = false

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
        isEnabled = true
        if (period < alwaysOnThreshold) {
            acquireWakelock(tag)
            isWakelockManaged = false
            timer.interval(period)
        } else {
            isWakelockManaged = true
            receiver.subscribe(this::onReceive)
            if (useOneTimeWorkers) {
                oneTimeWorker.start()
            } else {
                periodicWorker.interval(period)
            }
        }
        return START_STICKY_COMPATIBILITY
    }

    override fun onDestroy() {
        isEnabled = false
        releaseWakelock()
        receiver.unsubscribe(this::onReceive)
        timer.stop()
        if (useOneTimeWorkers){
            oneTimeWorker.cancel()
        } else {
            periodicWorker.cancel()
        }
        super.onDestroy()
    }

    private fun onReceive(@Suppress("UNUSED_PARAMETER") intent: Intent): Boolean {
        if (!isEnabled) {
            return false
        }
        timer.once(Duration.ZERO)
        if (useOneTimeWorkers) {
            oneTimeWorker.cancel()
            oneTimeWorker.once(period)
        }
        return true
    }
}
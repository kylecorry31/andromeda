package com.kylecorry.andromeda.background.services

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.kylecorry.andromeda.background.BroadcastWorker
import com.kylecorry.andromeda.background.TaskSchedulerFactory
import com.kylecorry.andromeda.core.system.BroadcastReceiverTopic
import com.kylecorry.luna.time.CoroutineTimer
import com.kylecorry.luna.time.ITimer
import java.time.Duration

/**
 * A base service for running a background task on an interval.
 * This is only recommended for foreground services, since background services may be killed by the OS more often.
 * @param workerThreshold Determines when to switch from to a scheduled worker. Scheduled jobs are inexact.
 * @param wakelockDuration The wakelock duration per update when not holding a continuous wakelock
 * @param useOneTimeWorkers Use one time workers instead of a periodic worker when over the worker threshold. Using one time workers can lead to slightly more on time intervals and it also allows variable durations.
 */
abstract class IntervalService(
    private val workerThreshold: Duration = Duration.ofMinutes(15),
    private val wakelockDuration: Duration? = null,
    private val useOneTimeWorkers: Boolean = false,
) : AndromedaService() {
    abstract val period: Duration
    protected open val holdWakelockWhenBelowThreshold: Boolean = true

    protected open fun getNonWorkerTimer(action: suspend () -> Unit): ITimer {
        return CoroutineTimer { action() }
    }

    private val receiver by lazy {
        BroadcastReceiverTopic(applicationContext, IntentFilter(action))
    }

    private val periodicWorker by lazy {
        TaskSchedulerFactory(this).interval(
            BroadcastWorker::class.java,
            uniqueId,
            Bundle().apply {
                putString("action", action)
            }
        )
    }

    private val oneTimeWorker by lazy {
        TaskSchedulerFactory(this).once(
            BroadcastWorker::class.java,
            uniqueId,
            Bundle().apply {
                putString("action", action)
            }
        )
    }

    private var isWakelockManaged = false
    private var isEnabled = false

    private val timer by lazy {
        getNonWorkerTimer {
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
    }

    abstract suspend fun doWork()

    open val action: String
        get() = "$tag.INTERVAL_ACTION"

    abstract val uniqueId: Int

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        isEnabled = true
        if (period < workerThreshold) {
            if (holdWakelockWhenBelowThreshold) {
                acquireWakelock(tag)
            }
            isWakelockManaged = !holdWakelockWhenBelowThreshold
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
        return START_STICKY
    }

    override fun onDestroy() {
        isEnabled = false
        releaseWakelock()
        receiver.unsubscribe(this::onReceive)
        timer.stop()
        if (useOneTimeWorkers) {
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

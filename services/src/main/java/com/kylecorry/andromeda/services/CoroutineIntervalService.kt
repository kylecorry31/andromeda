package com.kylecorry.andromeda.services

import android.content.Intent
import com.kylecorry.andromeda.core.time.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Duration

abstract class CoroutineIntervalService(val tag: String): ForegroundService() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private val intervalometer = Timer {
        serviceScope.launch {
            doWork()
        }
    }

    override fun onServiceStarted(intent: Intent?, flags: Int, startId: Int): Int {
        acquireWakelock(tag)
        intervalometer.interval(period)
        return START_STICKY
    }

    override fun onDestroy() {
        intervalometer.stop()
        serviceJob.cancel()
        super.onDestroy()
    }

    abstract suspend fun doWork()

    abstract val period: Duration
}
package com.kylecorry.andromeda.services

import android.content.Intent
import com.kylecorry.andromeda.core.time.Timer
import java.time.Duration

abstract class IntervalService(val tag: String): ForegroundService() {

    private val intervalometer = Timer {
        doWork()
    }

    override fun onServiceStarted(intent: Intent?, flags: Int, startId: Int): Int {
        acquireWakelock(tag)
        intervalometer.interval(period)
        return START_STICKY
    }

    override fun onDestroy() {
        intervalometer.stop()
        super.onDestroy()
    }

    abstract fun doWork()

    abstract val period: Duration
}
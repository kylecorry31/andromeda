package com.kylecorry.andromeda.core.time

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import java.time.Duration

class Timer(private val runnable: Runnable) {

    private var running = false
    private val handler = Handler(Looper.getMainLooper())

    private var intervalRunnable: Runnable? = null


    fun interval(periodMillis: Long, initialDelayMillis: Long = 0L) {
        if (running) {
            stop()
        }

        running = true

        val r = Runnable {
            runnable.run()
            val nextRunnable = intervalRunnable
            if (nextRunnable != null) {
                handler.postAtTime(nextRunnable, SystemClock.uptimeMillis() + periodMillis)
            }
        }

        intervalRunnable = r

        if (initialDelayMillis == 0L) {
            handler.post(r)
        } else {
            handler.postAtTime(r, SystemClock.uptimeMillis() + initialDelayMillis)
        }
    }

    fun interval(period: Duration, initialDelay: Duration = Duration.ZERO) {
        interval(period.toMillis(), initialDelay.toMillis())
    }

    fun once(delayMillis: Long) {
        if (running) {
            stop()
        }

        running = true
        if (delayMillis == 0L){
            handler.post(runnable)
        } else {
            handler.postAtTime(runnable, SystemClock.uptimeMillis() + delayMillis)
        }
    }

    fun once(delay: Duration) {
        once(delay.toMillis())
    }

    fun stop() {
        val iRunnable = intervalRunnable
        if (iRunnable != null) {
            handler.removeCallbacks(iRunnable)
        }
        intervalRunnable = null

        handler.removeCallbacks(runnable)
        running = false
    }

    fun isRunning(): Boolean {
        return running
    }

}
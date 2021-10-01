package com.kylecorry.andromeda.core.time

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.kylecorry.andromeda.core.coroutines.ControlledRunner
import com.kylecorry.andromeda.core.tryOrNothing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Duration

class CoroutineTimer(private val scope: CoroutineScope, private val action: suspend () -> Any) {

    private var running = false
    private val handler = Handler(Looper.getMainLooper())

    private var intervalRunnable: Runnable? = null

    private val runner = ControlledRunner<Any>()

    private val runnable = Runnable {
        scope.launch {
            runner.joinPreviousOrRun {
                action()
            }
        }
    }


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
        if (delayMillis == 0L) {
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

        tryOrNothing {
            runner.cancel()
        }

        running = false
    }

    fun isRunning(): Boolean {
        return running
    }

}
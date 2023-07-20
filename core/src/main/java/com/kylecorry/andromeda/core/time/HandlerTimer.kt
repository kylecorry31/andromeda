package com.kylecorry.andromeda.core.time

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.kylecorry.andromeda.core.coroutines.ControlledRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.util.Timer
import kotlin.coroutines.CoroutineContext

/**
 * A timer based on the Android Handler (based on uptime)
 */
class HandlerTimer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val observeOn: CoroutineContext = Dispatchers.Main,
    private val action: suspend () -> Unit
) : ITimer {

    private val runner = ControlledRunner<Any>()

    private var _isRunning = false
    private val handler = Handler(Looper.getMainLooper())

    private val runnable = Runnable {
        scope.launch {
            runner.cancelPreviousThenRun {
                withContext(observeOn) {
                    action()
                }
            }
        }
    }

    private var intervalRunnable: Runnable? = null

    override fun interval(period: Duration, initialDelay: Duration) {
        interval(period.toMillis(), initialDelay.toMillis())
    }

    override fun interval(periodMillis: Long, initialDelayMillis: Long) {
        if (isRunning()) {
            stop()
        }

        _isRunning = true

        val r = Runnable {
            runnable.run()
            val nextRunnable = intervalRunnable
            if (nextRunnable != null) {
                handler.postDelayed(nextRunnable, periodMillis)
            }
        }

        intervalRunnable = r

        if (initialDelayMillis == 0L) {
            handler.post(r)
        } else {
            handler.postDelayed(r, initialDelayMillis)
        }
    }

    override fun once(delay: Duration) {
        once(delay.toMillis())
    }

    override fun once(delayMillis: Long) {
        if (isRunning()) {
            stop()
        }

        _isRunning = true
        if (delayMillis == 0L) {
            handler.post(runnable)
        } else {
            handler.postAtTime(runnable, SystemClock.uptimeMillis() + delayMillis)
        }
    }

    override fun stop() {
        _isRunning = false
        val iRunnable = intervalRunnable
        if (iRunnable != null) {
            handler.removeCallbacks(iRunnable)
        }
        intervalRunnable = null

        handler.removeCallbacks(runnable)
    }

    override fun isRunning(): Boolean {
        return _isRunning
    }
}
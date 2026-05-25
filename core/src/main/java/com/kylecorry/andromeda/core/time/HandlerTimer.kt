package com.kylecorry.andromeda.core.time

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.kylecorry.luna.concurrency.CoroutineQueueRunner
import com.kylecorry.luna.time.ITimer
import com.kylecorry.luna.time.TimerActionBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.ApiStatus.Experimental
import java.time.Duration
import kotlin.coroutines.CoroutineContext

/**
 * A timer based on the Android Handler (based on uptime)
 * @param scope The scope to run the action on
 * @param observeOn The context to observe the action on
 * @param looper The looper to run the timer on
 * @param actionBehavior The behavior to use when the action is already running when the timer is triggered (periodic only).
 * @param action The action to run
 */
@Experimental
class HandlerTimer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val observeOn: CoroutineContext = Dispatchers.Main,
    looper: Looper = Looper.getMainLooper(),
    private val actionBehavior: TimerActionBehavior = TimerActionBehavior.Replace,
    private val action: suspend () -> Unit
) : ITimer {

    private val runner = CoroutineQueueRunner(scope = scope, dispatcher = observeOn)

    private var _isRunning = false
    private val handler = Handler(looper)

    private val runnable = Runnable {
        scope.launch {
            if (actionBehavior == TimerActionBehavior.Wait) {
                withContext(observeOn) {
                    action()
                }
            } else if (actionBehavior == TimerActionBehavior.Skip) {
                runner.skipIfRunning {
                    action()
                }
            } else if (actionBehavior == TimerActionBehavior.Replace) {
                runner.replace {
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
        runner.cancel()
    }

    override fun isRunning(): Boolean {
        return _isRunning
    }
}

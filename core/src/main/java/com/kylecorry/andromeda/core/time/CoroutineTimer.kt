package com.kylecorry.andromeda.core.time

import android.os.SystemClock
import com.kylecorry.andromeda.core.coroutines.ControlledRunner
import kotlinx.coroutines.*
import java.time.Duration
import kotlin.coroutines.CoroutineContext

/**
 * A timer based on coroutine delays (based on uptime)
 * @param scope The scope to run the timer on
 * @param observeOn The context to observe the action on
 * @param waitForAction If true, the timer will wait for the action to complete before starting the next interval (variable interval), otherwise the timer will attempt to maintain a constant interval (skipping intervals if the action takes too long). This behavior may be replaced by TimerActionBehavior.
 * @param action The action to run
 */
class CoroutineTimer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val observeOn: CoroutineContext = Dispatchers.Main,
    private val waitForAction: Boolean = true,
    private val action: suspend () -> Unit
) : ITimer {

    private val runner = ControlledRunner<Any>()

    private var _isRunning = false

    override fun interval(period: Duration, initialDelay: Duration) {
        interval(period.toMillis(), initialDelay.toMillis())
    }

    override fun interval(periodMillis: Long, initialDelayMillis: Long) {
        _isRunning = true
        scope.launch {
            runner.cancelPreviousThenRun {
                if (initialDelayMillis > 0) {
                    delay(initialDelayMillis)
                }
                while (isRunning()) {
                    val startTime = SystemClock.uptimeMillis()
                    withContext(observeOn) {
                        action()
                    }
                    val endTime = SystemClock.uptimeMillis()
                    val actionTime = endTime - startTime
                    val period =
                        periodMillis - if (waitForAction) 0 else (actionTime % periodMillis)
                    if (period > 0) {
                        delay(period)
                    } else if (periodMillis > 0) {
                        delay(periodMillis)
                    }
                }

            }
        }
    }

    override fun once(delay: Duration) {
        once(delay.toMillis())
    }

    override fun once(delayMillis: Long) {
        _isRunning = true
        scope.launch {
            runner.cancelPreviousThenRun {
                if (delayMillis > 0) {
                    delay(delayMillis)
                }
                withContext(observeOn) {
                    action()
                }
            }
        }
    }

    override fun stop() {
        _isRunning = false
        runner.cancel()
    }

    override fun isRunning(): Boolean {
        return _isRunning
    }
}
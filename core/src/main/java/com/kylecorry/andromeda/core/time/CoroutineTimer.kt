package com.kylecorry.andromeda.core.time

import com.kylecorry.andromeda.core.coroutines.ControlledRunner
import kotlinx.coroutines.*
import java.time.Duration
import kotlin.coroutines.CoroutineContext

/**
 * A timer based on coroutine delays (based on uptime)
 */
class CoroutineTimer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val observeOn: CoroutineContext = Dispatchers.Main,
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
                    withContext(observeOn) {
                        action()
                    }
                    if (periodMillis > 0) {
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
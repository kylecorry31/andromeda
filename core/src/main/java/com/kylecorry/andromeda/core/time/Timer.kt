package com.kylecorry.andromeda.core.time

import com.kylecorry.andromeda.core.coroutines.ControlledRunner
import kotlinx.coroutines.*
import java.time.Duration
import kotlin.coroutines.CoroutineContext

class Timer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val observeOn: CoroutineContext = Dispatchers.Main,
    private val action: suspend () -> Unit
) {

    private val runner = ControlledRunner<Any>()

    private var _isRunning = false

    fun interval(period: Duration, initialDelay: Duration = Duration.ZERO) {
        interval(period.toMillis(), initialDelay.toMillis())
    }

    fun interval(periodMillis: Long, initialDelayMillis: Long = 0L) {
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

    fun once(delay: Duration) {
        once(delay.toMillis())
    }

    fun once(delayMillis: Long) {
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

    fun stop() {
        _isRunning = false
        runner.cancel()
    }

    fun isRunning(): Boolean {
        return _isRunning
    }
}
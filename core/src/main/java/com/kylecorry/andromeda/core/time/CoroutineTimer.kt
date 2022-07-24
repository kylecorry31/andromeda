package com.kylecorry.andromeda.core.time

import com.kylecorry.andromeda.core.coroutines.ControlledRunner
import com.kylecorry.andromeda.core.tryOrLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Duration

class CoroutineTimer(private val scope: CoroutineScope, private val action: suspend () -> Any) {

    private val timer = Timer {
        scope.launch {
            runner.joinPreviousOrRun {
                action()
            }
        }
    }

    private val runner = ControlledRunner<Any>()

    fun interval(periodMillis: Long, initialDelayMillis: Long = 0L) {
        timer.interval(periodMillis, initialDelayMillis)
    }

    fun interval(period: Duration, initialDelay: Duration = Duration.ZERO) {
        timer.interval(period, initialDelay)
    }

    fun once(delayMillis: Long) {
        timer.once(delayMillis)
    }

    fun once(delay: Duration) {
        timer.once(delay)
    }

    fun stop() {
        timer.stop()
        tryOrLog {
            runner.cancel()
        }
    }

    fun isRunning(): Boolean {
        return timer.isRunning()
    }

}
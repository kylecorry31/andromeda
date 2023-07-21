package com.kylecorry.andromeda.core.time

import com.kylecorry.andromeda.core.coroutines.ControlledRunner
import com.kylecorry.andromeda.core.tryOrNothing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.util.Timer
import kotlin.coroutines.CoroutineContext

/**
 * A timer based on the Java Timer (based on system time)
 */
class JavaTimer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val observeOn: CoroutineContext = Dispatchers.Main,
    private val actionBehavior: TimerActionBehavior = TimerActionBehavior.Replace,
    private val action: suspend () -> Unit
) : ITimer {

    private val runner = ControlledRunner<Any>()

    private var _isRunning = false
    private val timer = Timer()

    private val task = object : java.util.TimerTask() {
        override fun run() {
            scope.launch {
                runner.run(actionBehavior) {
                    withContext(observeOn) {
                        action()
                    }
                }
            }
        }
    }

    override fun interval(period: Duration, initialDelay: Duration) {
        interval(period.toMillis(), initialDelay.toMillis())
    }

    override fun interval(periodMillis: Long, initialDelayMillis: Long) {
        if (_isRunning) {
            tryOrNothing {
                timer.cancel()
            }
        }
        _isRunning = true
        timer.scheduleAtFixedRate(task, initialDelayMillis, periodMillis)
    }

    override fun once(delay: Duration) {
        once(delay.toMillis())
    }

    override fun once(delayMillis: Long) {
        if (_isRunning) {
            tryOrNothing {
                timer.cancel()
            }
        }
        _isRunning = true
        timer.schedule(task, delayMillis)
    }

    override fun stop() {
        if (_isRunning){
            timer.cancel()
        }
        _isRunning = false
        runner.cancel()
    }

    override fun isRunning(): Boolean {
        return _isRunning
    }
}
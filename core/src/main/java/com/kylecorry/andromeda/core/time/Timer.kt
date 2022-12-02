package com.kylecorry.andromeda.core.time

import java.time.Duration
import kotlin.math.min

class Timer(private val runnable: Runnable) {

    private val minInterval = Duration.ofMinutes(1).toMillis()

    private val timer = TimerHelper {
        onTimer()
    }

    private var remainingTime = 0L
    private var intervalTime = 0L
    private var lastCalled = 0L

    private fun onTimer() {
        if (!isRunning()) {
            return
        }

        updateRemainingTime()

        if (remainingTime <= 0) {
            runnable.run()
            if (intervalTime >= 0L) {
                schedule(intervalTime, intervalTime)
            }
        } else {
            schedule(remainingTime, intervalTime)
        }
    }

    fun interval(periodMillis: Long, initialDelayMillis: Long = 0L) {
        schedule(initialDelayMillis, periodMillis)
    }

    fun interval(period: Duration, initialDelay: Duration = Duration.ZERO) {
        interval(period.toMillis(), initialDelay.toMillis())
    }

    fun once(delayMillis: Long) {
        schedule(delayMillis, -1L)
    }

    fun once(delay: Duration) {
        once(delay.toMillis())
    }

    fun stop() {
        timer.stop()
    }

    fun isRunning(): Boolean {
        return timer.isRunning()
    }

    private fun schedule(delay: Long, interval: Long) {
        lastCalled = System.currentTimeMillis()
        remainingTime = delay
        intervalTime = interval
        timer.once(getNextDelay())
    }

    private fun getNextDelay(): Long {
        return min(remainingTime, minInterval)
    }

    private fun updateRemainingTime() {
        val diff = System.currentTimeMillis() - lastCalled
        remainingTime -= diff
        if (remainingTime < 0L) {
            remainingTime = 0L
        }
    }

}
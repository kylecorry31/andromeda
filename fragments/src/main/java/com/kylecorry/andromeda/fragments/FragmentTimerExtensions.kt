package com.kylecorry.andromeda.fragments

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kylecorry.andromeda.core.time.CoroutineTimer
import java.time.Duration

fun Fragment.interval(
    interval: Long,
    delay: Long = 0L,
    action: suspend () -> Unit
): LifecycleEventObserver {
    val timer = CoroutineTimer(action = action)
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            timer.interval(interval, delay)
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            timer.stop()
        }
    }
    viewLifecycleOwner.lifecycle.addObserver(observer)
    return observer
}

fun Fragment.interval(
    interval: Duration,
    delay: Duration = Duration.ZERO,
    action: suspend () -> Unit
): LifecycleEventObserver {
    return interval(interval.toMillis(), delay.toMillis(), action)
}

fun Fragment.once(
    delay: Long = 0L,
    action: suspend () -> Unit
): LifecycleEventObserver {
    val timer = CoroutineTimer(action = action)
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            timer.once(delay)
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            timer.stop()
        }
    }
    viewLifecycleOwner.lifecycle.addObserver(observer)
    return observer
}

fun Fragment.once(
    delay: Duration = Duration.ZERO,
    action: suspend () -> Unit
): LifecycleEventObserver {
    return once(delay.toMillis(), action)
}
package com.kylecorry.andromeda.core.time

import com.kylecorry.andromeda.core.coroutines.ControlledRunner

internal suspend fun <T> ControlledRunner<T>.run(
    behavior: TimerActionBehavior,
    action: suspend () -> T
) {
    when (behavior) {
        TimerActionBehavior.Skip -> {
            joinPreviousOrRun(action)
        }
        TimerActionBehavior.Replace -> {
            cancelPreviousThenRun(action)
        }
        else -> {
            // Do nothing - wait is not supported by the controlled runner
        }
    }
}
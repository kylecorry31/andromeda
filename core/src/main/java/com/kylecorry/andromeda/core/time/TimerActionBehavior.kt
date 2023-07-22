package com.kylecorry.andromeda.core.time

enum class TimerActionBehavior {
    /**
     * If an action is already running, skip the new action until the next interval
     */
    Skip,

    /**
     * If an action is already running, cancel it and run the new action
     */
    Replace,

    /**
     * Wait for the action to finish before starting the next interval (leads to variable intervals)
     */
    Wait
}
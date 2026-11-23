package com.kylecorry.andromeda.sense

import android.os.Handler
import android.os.HandlerThread

/**
 * A shared background thread for sensors whose event handling should stay off the main thread.
 * Sensors using it notify their listeners on this thread.
 */
object SensorThread {
    val handler: Handler by lazy {
        val thread = HandlerThread("andromeda-sensors").apply { start() }
        Handler(thread.looper)
    }
}

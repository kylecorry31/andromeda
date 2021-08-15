package com.kylecorry.andromeda.core.system

import android.os.Looper
import java.time.Duration
import kotlin.concurrent.thread
import kotlin.system.exitProcess

object ExceptionUtils {

    fun onUncaughtException(waitTime: Duration, exceptionHandler: (throwable: Throwable) -> Unit) {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            thread {
                Looper.prepare()
                exceptionHandler(throwable)
                Looper.loop()
            }

            try {
                Thread.sleep(waitTime.toMillis())
            } catch (e: InterruptedException) {
            }

            exitProcess(2)
        }
    }

}
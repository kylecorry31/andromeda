package com.kylecorry.andromeda.exceptions

import android.os.Looper
import com.kylecorry.andromeda.core.system.CurrentApp
import com.kylecorry.andromeda.core.tryOrNothing
import java.time.Duration
import kotlin.concurrent.thread

object Exceptions {

    fun wrapOnUncaughtException(exceptionHandler: (throwable: Throwable) -> Unit) {
        val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                exceptionHandler(throwable)
            } finally {
                originalHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    fun onUncaughtException(exceptionHandler: (throwable: Throwable) -> Unit) {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            exceptionHandler(throwable)
        }
    }

    fun onUncaughtException(waitTime: Duration, exceptionHandler: (throwable: Throwable) -> Unit) {
        onUncaughtException { throwable ->
            thread {
                Looper.prepare()
                exceptionHandler(throwable)
                Looper.loop()
            }

            tryOrNothing {
                Thread.sleep(waitTime.toMillis())
            }

            CurrentApp.kill()
        }
    }


}
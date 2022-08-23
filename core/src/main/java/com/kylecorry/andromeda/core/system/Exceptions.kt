package com.kylecorry.andromeda.core.system

import android.os.Looper
import com.kylecorry.andromeda.core.tryOrNothing
import java.time.Duration
import kotlin.concurrent.thread
import kotlin.system.exitProcess

object Exceptions {

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
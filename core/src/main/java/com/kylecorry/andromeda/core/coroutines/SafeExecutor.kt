package com.kylecorry.andromeda.core.coroutines

import android.util.Log
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class SafeExecutor(
    private val delegate: Executor,
    private val onError: (Throwable) -> Unit = defaultThrowableHandler
) : Executor {
    override fun execute(command: Runnable) {
        delegate.execute {
            try {
                command.run()
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }

    companion object {

        private val defaultThrowableHandler: (Throwable) -> Unit = {
            Log.e("SafeExecutor", "Exception caught in executor: ${it.message}", it)
        }

        fun newSingleThreadExecutor(onError: (Throwable) -> Unit = defaultThrowableHandler): SafeExecutor {
            return SafeExecutor(Executors.newSingleThreadExecutor(), onError)
        }
    }
}
package com.kylecorry.andromeda.exceptions

import android.content.Context
import com.kylecorry.andromeda.core.system.CurrentApp
import com.kylecorry.andromeda.core.tryOrLog
import com.kylecorry.andromeda.files.LocalFiles

abstract class BaseExceptionHandler(
    protected val context: Context,
    private val generator: IBugReportGenerator,
    private val filename: String = "errors/error.txt"
) {

    fun bind() {
        if (!LocalFiles.getFile(context, filename, create = false).exists()) {
            setupHandler()
        }
        handleLastException()
    }

    abstract fun handleBugReport(log: String)

    private fun handleLastException() {
        val file = LocalFiles.getFile(context, filename, create = false)
        if (!file.exists()) {
            return
        }
        val error = LocalFiles.read(context, filename)
        LocalFiles.delete(context, filename)

        handleBugReport(error)
        setupHandler()
    }

    private fun setupHandler() {
        Exceptions.onUncaughtException {
            recordException(it)
            tryOrLog {
                CurrentApp.restart(context)
            }
        }
    }

    private fun recordException(throwable: Throwable) {
        val details = generator.generate(context, throwable)
        LocalFiles.write(context, filename, details, false)
    }

}
package com.kylecorry.andromeda.exceptions

import android.content.Context
import com.kylecorry.andromeda.core.system.CurrentApp
import com.kylecorry.andromeda.core.tryOrLog
import com.kylecorry.andromeda.files.IFileSystem
import com.kylecorry.andromeda.files.LocalFileSystem

abstract class BaseExceptionHandler(
    protected val context: Context,
    private val generator: IBugReportGenerator,
    private val filename: String = "errors/error.txt",
    private val fileSystem: IFileSystem = LocalFileSystem(context),
    private val shouldRestartApp: Boolean = true,
    private val shouldWrapSystemExceptionHandler: Boolean = false
) {

    fun bind() {
        if (!fileSystem.getFile(filename, create = false).exists()) {
            setupHandler()
        }
        handleLastException()
    }

    abstract fun handleBugReport(log: String)

    private fun handleLastException() {
        val file = fileSystem.getFile(filename, create = false)
        if (!file.exists()) {
            return
        }
        val error = fileSystem.read(filename)
        fileSystem.delete(filename)

        handleBugReport(error)
        setupHandler()
    }

    private fun setupHandler() {
        val handler = { throwable: Throwable ->
            recordException(throwable)
            if (shouldRestartApp) {
                tryOrLog {
                    CurrentApp.restart(context)
                }
            }
        }

        if (shouldWrapSystemExceptionHandler) {
            Exceptions.wrapOnUncaughtException(handler)
        } else {
            Exceptions.onUncaughtException(handler)
        }
    }

    private fun recordException(throwable: Throwable) {
        val details = generator.generate(context, throwable)
        fileSystem.write(filename, details, false)
    }

}
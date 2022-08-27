package com.kylecorry.andromeda.exceptions

import android.content.Context
import com.kylecorry.andromeda.alerts.Alerts
import com.kylecorry.andromeda.core.system.CurrentApp
import com.kylecorry.andromeda.core.system.Intents
import com.kylecorry.andromeda.core.tryOrLog
import com.kylecorry.andromeda.files.LocalFiles

object EmailExceptionHandler {

    fun initialize(
        context: Context,
        message: BugReportMessage,
        filename: String = "errors/error.txt"
    ) {
        if (!LocalFiles.getFile(context, filename, create = false).exists()) {
            setupHandler(context, filename, message.generator)
        }
        handleLastException(context, filename, message)
    }

    private fun handleLastException(
        context: Context,
        filename: String,
        message: BugReportMessage
    ) {
        val file = LocalFiles.getFile(context, filename, create = false)
        if (!file.exists()) {
            return
        }
        val body = LocalFiles.read(context, filename)
        LocalFiles.delete(context, filename)

        Alerts.dialog(
            context,
            message.title,
            message.description,
            okText = message.emailAction,
            cancelText = message.ignoreAction
        ) { cancelled ->
            if (!cancelled) {
                val intent = Intents.email(
                    message.emailAddress,
                    message.emailSubject,
                    body
                )

                context.startActivity(intent)
            } else {
                setupHandler(context, filename, message.generator)
            }
        }
    }

    private fun setupHandler(context: Context, filename: String, generator: IBugReportGenerator) {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            recordException(context, throwable, filename, generator)
            tryOrLog {
                CurrentApp.restart(context)
            }
        }
    }

    private fun recordException(
        context: Context,
        throwable: Throwable,
        filename: String,
        generator: IBugReportGenerator
    ) {
        val details = generator.generate(context, throwable)
        LocalFiles.write(context, filename, details, false)
    }

}
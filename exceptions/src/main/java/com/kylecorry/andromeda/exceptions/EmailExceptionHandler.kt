package com.kylecorry.andromeda.exceptions

import android.content.Context
import com.kylecorry.andromeda.alerts.Alerts
import com.kylecorry.andromeda.core.system.Intents

class EmailExceptionHandler(
    context: Context,
    generator: IBugReportGenerator,
    filename: String = "errors/error.txt",
    private val messageProvider: (context: Context, log: String) -> BugReportEmailMessage,
) : BaseExceptionHandler(context, generator, filename) {

    override fun handleBugReport(log: String) {
        val message = messageProvider(context, log)
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
                    log
                )

                context.startActivity(intent)
            }
        }
    }
}
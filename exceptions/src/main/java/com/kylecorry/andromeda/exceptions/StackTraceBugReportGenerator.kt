package com.kylecorry.andromeda.exceptions

import android.content.Context

class StackTraceBugReportGenerator : IBugReportGenerator {
    override fun generate(context: Context, throwable: Throwable): String {
        val message = throwable.message ?: ""
        val stackTrace = throwable.stackTraceToString()
        return "Message: ${message}\n\n$stackTrace"
    }
}
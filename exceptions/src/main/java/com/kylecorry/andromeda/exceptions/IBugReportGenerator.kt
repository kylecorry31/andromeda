package com.kylecorry.andromeda.exceptions

import android.content.Context

interface IBugReportGenerator {
    fun generate(context: Context, throwable: Throwable): String
}
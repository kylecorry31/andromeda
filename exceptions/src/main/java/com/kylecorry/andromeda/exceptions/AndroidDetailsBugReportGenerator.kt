package com.kylecorry.andromeda.exceptions

import android.content.Context
import android.os.Build

class AndroidDetailsBugReportGenerator : IBugReportGenerator {
    override fun generate(context: Context, throwable: Throwable): String {
        val androidVersion = Build.VERSION.SDK_INT
        return "Android SDK: $androidVersion"
    }
}
package com.kylecorry.andromeda.exceptions

import android.content.Context
import com.kylecorry.andromeda.core.system.Package

class AppDetailsBugReportGenerator(private val appName: String) : IBugReportGenerator {
    override fun generate(context: Context, throwable: Throwable): String {
        val appVersion = Package.getVersionName(context)
        val isDebug = BuildConfig.DEBUG
        return "$appName $appVersion${if (isDebug) " (Debug)" else ""}"
    }
}
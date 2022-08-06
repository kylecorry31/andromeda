package com.kylecorry.andromeda.core.system

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.kylecorry.andromeda.core.tryOrDefault

object Package {

    fun getPackageName(context: Context): String {
        return context.packageName
    }

    fun getVersionName(context: Context): String {
        return getPackageInfo(context, getPackageName(context)).versionName
    }

    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return tryOrDefault(false) {
            getPackageInfo(context, packageName)
            true
        }
    }

    fun openApp(context: Context, packageName: String) {
        if (!isPackageInstalled(context, packageName)) return
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun setComponentEnabled(context: Context, className: String, enabled: Boolean) {
        val compName = ComponentName(
            getPackageName(context),
            className
        )
        context.packageManager.setComponentEnabledSetting(
            compName,
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun getPackageInfo(
        context: Context,
        packageName: String,
        flags: Int = 0
    ): PackageInfo {
        return if (Build.VERSION.SDK_INT >= 33) {
            context.packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(flags.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(packageName, flags)
        }
    }

}
package com.kylecorry.andromeda.core.system

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.kylecorry.andromeda.core.tryOrDefault
import java.security.MessageDigest

object Package {

    fun getPackageName(context: Context): String {
        return context.packageName
    }

    fun getVersionName(context: Context): String {
        return getPackageInfo(context, getPackageName(context)).versionName ?: ""
    }

    fun getVersionCode(context: Context): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getPackageInfo(context, getPackageName(context)).longVersionCode
        } else {
            @Suppress("DEPRECATION")
            getPackageInfo(context, getPackageName(context)).versionCode.toLong()
        }
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

    fun getPackageInfo(
        context: Context,
        packageName: String = getPackageName(context),
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

    fun getSelfSignatureSha256Fingerprints(context: Context): List<String> {
        return getSignatureSha256Fingerprints(context, context.packageName)
    }

    @Suppress("DEPRECATION")
    fun getSignatureSha256Fingerprints(
        context: Context,
        packageName: String
    ): List<String> {
        val info = tryOrDefault(null) {
            getPackageInfo(
                context,
                packageName,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
            )
        } ?: return emptyList()
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = info.signingInfo ?: return emptyList()
            if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }
        } else {
            info.signatures ?: return emptyList()
        }
        val digest = MessageDigest.getInstance("SHA-256")
        val signatureHashes = mutableListOf<String>()
        for (sig in signatures) {
            val digest = digest.digest(sig.toByteArray())
            val hash = digest.joinToString(":") { String.format("%02X", it) }
            signatureHashes.add(hash)
        }
        return signatureHashes.distinct()
    }

}
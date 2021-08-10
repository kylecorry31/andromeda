package com.kylecorry.andromeda.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import com.kylecorry.andromeda.core.system.PackageUtils

class PermissionService(private val context: Context) {

    fun isBackgroundLocationEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            canGetFineLocation()
        } else {
            hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    fun canGetFineLocation(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun canGetCoarseLocation(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    fun canUseFlashlight(): Boolean {
        return hasPermission("android.permission.FLASHLIGHT")
    }

    fun isCameraEnabled(): Boolean {
        return hasPermission(Manifest.permission.CAMERA)
    }

    fun canUseBluetooth(): Boolean {
        return hasPermission(Manifest.permission.BLUETOOTH)
    }

    fun canVibrate(): Boolean {
        return hasPermission(Manifest.permission.VIBRATE)
    }

    fun isIgnoringBatteryOptimizations(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService<PowerManager>()
                ?.isIgnoringBatteryOptimizations(PackageUtils.getPackageName(context)) ?: false
        } else {
            true
        }
    }

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions(activity: Activity, permissions: List<String>, requestCode: Int) {
        val notGrantedPermissions = permissions.filterNot { hasPermission(it) }
        if (notGrantedPermissions.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.onRequestPermissionsResult(
                requestCode,
                permissions.toTypedArray(),
                intArrayOf(PackageManager.PERMISSION_GRANTED)
            )
            return
        }
        if (notGrantedPermissions.isEmpty()) {
            // On older versions of Android this will call the callback method
            ActivityCompat.requestPermissions(
                activity,
                permissions.toTypedArray(),
                requestCode
            )
            return
        }
        ActivityCompat.requestPermissions(
            activity,
            notGrantedPermissions.toTypedArray(),
            requestCode
        )
    }

    fun requestPermissions(fragment: Fragment, permissions: List<String>, requestCode: Int) {
        // TODO: Use the registerForActivityResult method instead
        val notGrantedPermissions =
            permissions.filterNot { hasPermission(it) }
        if (notGrantedPermissions.isEmpty()) {
            fragment.onRequestPermissionsResult(
                requestCode,
                permissions.toTypedArray(),
                intArrayOf(PackageManager.PERMISSION_GRANTED)
            )
            return
        }
        fragment.requestPermissions(
            notGrantedPermissions.toTypedArray(),
            requestCode
        )
    }
}
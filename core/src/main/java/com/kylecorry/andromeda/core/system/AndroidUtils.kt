package com.kylecorry.andromeda.core.system

import android.os.Build

object AndroidUtils {
    val sdk = Build.VERSION.SDK_INT
    val androidVersion = Build.VERSION.RELEASE
    val manufacturer = Build.MANUFACTURER
    val device = Build.PRODUCT
    val model = Build.MODEL
    val fullDeviceName = "$manufacturer $device"
}
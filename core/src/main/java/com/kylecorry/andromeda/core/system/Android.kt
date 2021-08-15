package com.kylecorry.andromeda.core.system

import android.os.Build

object Android {
    val sdk = Build.VERSION.SDK_INT
    val version = Build.VERSION.RELEASE
    val manufacturer = Build.MANUFACTURER
    val device = Build.PRODUCT
    val model = Build.MODEL
    val fullDeviceName = "$manufacturer $device"
}
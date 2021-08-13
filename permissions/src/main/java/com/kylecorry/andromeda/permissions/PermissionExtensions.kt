package com.kylecorry.andromeda.permissions

import android.app.Activity
import androidx.core.app.ActivityCompat

// TODO: Find a way to call this with an action parameter
fun Activity.requestPermissions(permissions: List<String>, requestCode: Int) {
    if (permissions.isEmpty()) {
        // On older versions of Android this will call the callback method
        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            requestCode
        )
        return
    }
    ActivityCompat.requestPermissions(
        this,
        permissions.toTypedArray(),
        requestCode
    )
}
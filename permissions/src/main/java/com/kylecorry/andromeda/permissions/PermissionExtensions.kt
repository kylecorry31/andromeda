package com.kylecorry.andromeda.permissions

import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

fun Fragment.requestPermissions(permissions: List<String>, action: () -> Unit) {
    if (permissions.isEmpty()) {
        action()
        return
    }
    val launcher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        action()
    }

    launcher.launch(permissions.toTypedArray())
}

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
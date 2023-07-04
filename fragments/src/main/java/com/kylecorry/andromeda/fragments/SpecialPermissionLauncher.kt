package com.kylecorry.andromeda.fragments

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.kylecorry.andromeda.alerts.Alerts
import com.kylecorry.andromeda.permissions.PermissionRationale
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.andromeda.permissions.SpecialPermission

internal class SpecialPermissionLauncher(
    private val context: Context,
    lifecycle: LifecycleOwner
) {

    private var action: (() -> Unit)? = null

    private val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            onResume()
        }
    }

    init {
        lifecycle.lifecycle.addObserver(observer)
    }

    fun requestPermission(
        permission: SpecialPermission,
        rationale: PermissionRationale,
        action: () -> Unit
    ) {
        if (Permissions.hasPermission(context, permission)) {
            action()
            return
        }

        Alerts.dialog(
            context,
            rationale.title,
            rationale.message,
            cancelText = rationale.cancel ?: context.getString(android.R.string.cancel),
            okText = rationale.ok ?: context.getString(android.R.string.ok)
        ) { cancelled ->
            if (!cancelled) {
                this.action = action
                Permissions.requestPermission(context, permission)
            } else {
                action()
            }
        }
    }

    private fun onResume(){
        action?.invoke()
        action = null
    }
}
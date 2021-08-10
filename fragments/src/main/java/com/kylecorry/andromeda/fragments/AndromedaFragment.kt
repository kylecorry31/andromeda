package com.kylecorry.andromeda.fragments

import androidx.fragment.app.Fragment
import com.kylecorry.andromeda.permissions.PermissionService

open class AndromedaFragment : Fragment() {

    private val permissions by lazy { PermissionService(requireContext()) }
    private val permissionActions = mutableMapOf<Int, () -> Unit>()

    protected fun requestPermissions(
        requestCode: Int,
        permissions: List<String>,
        action: () -> Unit
    ) {
        if (permissions.all { this.permissions.hasPermission(it) }) {
            action.invoke()
            return
        }

        permissionActions[requestCode] = action
        this.permissions.requestPermissions(
            this,
            permissions,
            requestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val action = permissionActions[requestCode]
        permissionActions.remove(requestCode)
        action?.invoke()
    }
}
package com.kylecorry.andromeda.fragments

import com.kylecorry.andromeda.permissions.PermissionRationale
import com.kylecorry.andromeda.permissions.SpecialPermission

interface IPermissionRequester {
    fun requestPermissions(permissions: List<String>, action: () -> Unit)

    /**
     * Request a special permission - only supports one at a time
     * @param permission the permission to request
     * @param rationale the rationale for the permission
     * @param action the action to run after the user grants/denies the permission
     */
    fun requestPermission(
        permission: SpecialPermission,
        rationale: PermissionRationale,
        action: () -> Unit
    )
}
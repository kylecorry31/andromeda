package com.kylecorry.andromeda.permissions

data class NoPermissionException(val requiredPermission: String) :
    Exception("Permission $requiredPermission is required to perform this operation")
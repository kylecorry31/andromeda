package com.kylecorry.andromeda.permissions

data class PermissionRationale(
    val title: String,
    val message: CharSequence,
    val cancel: String? = null,
    val ok: String? = null
)

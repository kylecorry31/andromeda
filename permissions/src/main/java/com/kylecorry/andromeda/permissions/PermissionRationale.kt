package com.kylecorry.andromeda.permissions

data class PermissionRationale(
    val title: String,
    val message: CharSequence,
    val ok: String? = null,
    val cancel: String? = null
)

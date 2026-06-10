package com.kylecorry.andromeda.core.system

data class UriAccess(
    val requirePersistentAccess: Boolean = false,
    val requireReadAccess: Boolean = true,
    val requireWriteAccess: Boolean = false
)

package com.kylecorry.andromeda.fragments

interface IPermissionRequester {
    fun requestPermissions(permissions: List<String>, action: () -> Unit)
}
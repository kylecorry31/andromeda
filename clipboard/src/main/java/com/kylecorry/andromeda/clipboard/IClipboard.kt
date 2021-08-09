package com.kylecorry.andromeda.clipboard

interface IClipboard {
    fun copy(text: String, toastMessage: String? = null)
}
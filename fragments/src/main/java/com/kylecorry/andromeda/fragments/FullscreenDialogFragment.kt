package com.kylecorry.andromeda.fragments

import androidx.fragment.app.DialogFragment

open class FullscreenDialogFragment : DialogFragment() {
    override fun getTheme(): Int {
        return androidx.appcompat.R.style.ThemeOverlay_AppCompat
    }
}
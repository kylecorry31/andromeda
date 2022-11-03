package com.kylecorry.andromeda.fragments

import androidx.fragment.app.DialogFragment

open class FullscreenDialogFragment : DialogFragment() {
    override fun getTheme(): Int {
        return R.style.ThemeOverlay_AppCompat
    }
}
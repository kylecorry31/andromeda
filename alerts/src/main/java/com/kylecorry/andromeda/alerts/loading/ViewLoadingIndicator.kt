package com.kylecorry.andromeda.alerts.loading

import android.view.View
import androidx.core.view.isVisible

class ViewLoadingIndicator(private val view: View) : ILoadingIndicator {
    override fun show() {
        view.isVisible = true
    }

    override fun hide() {
        view.isVisible = false
    }
}
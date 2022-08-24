package com.kylecorry.andromeda.alerts.loading

class ResettableLoadingIndicator(private val loadingIndicator: ILoadingIndicator) :
    ILoadingIndicator {

    private var hasRun = false

    override fun show() {
        if (!hasRun) {
            loadingIndicator.show()
            hasRun = true
        }
    }

    override fun hide() {
        loadingIndicator.hide()
    }

    fun reset() {
        hasRun = false
    }
}
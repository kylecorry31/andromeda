package com.kylecorry.andromeda.core.ui

import com.kylecorry.andromeda.core.topics.generic.ITopic

interface IProgressReporter {
    val progress: ITopic<Progress>
}

data class Progress(val processed: Int, val total: Int, val message: String? = null) {
    val percent: Float
        get() = processed.toFloat() / total.toFloat()
}
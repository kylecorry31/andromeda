package com.kylecorry.andromeda.core.system

import android.content.Intent

interface IntentResultRetriever {
    fun getResult(intent: Intent, action: (successful: Boolean, data: Intent?) -> Unit)
}
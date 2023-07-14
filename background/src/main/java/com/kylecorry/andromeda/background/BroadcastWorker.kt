package com.kylecorry.andromeda.background

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kylecorry.andromeda.core.system.Intents

/**
 * A worker that broadcasts an intent
 * Input data can contain:
 * - action: The action to broadcast
 * - type: The type of the intent (optional)
 * - category: The category of the intent (optional)
 * - dataUri: The data URI of the intent (optional)
 */
class BroadcastWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val action = inputData.getString("action") ?: return Result.failure()
        val category = inputData.getString("category")
        val type = inputData.getString("type")
        val dataUri = inputData.getString("dataUri")
        val intent = Intents.localIntent(applicationContext, action)
        if (category != null) {
            intent.addCategory(category)
        }
        intent.type = type
        if (dataUri != null) {
            intent.data = dataUri.toUri()
        }
        applicationContext.sendBroadcast(intent)
        return Result.success()
    }
}
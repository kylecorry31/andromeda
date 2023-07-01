package com.kylecorry.andromeda.background

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.kylecorry.andromeda.core.system.Intents

class ServiceTaskScheduler(
    private val context: Context,
    private val service: Class<out Service>,
    private val foreground: Boolean = false,
    private val input: Bundle? = null
) : IAlwaysOnTaskScheduler {

    override fun start() {
        val intent = getIntent()
        Intents.startService(context, intent, foreground)
    }

    override fun cancel() {
        context.stopService(getIntent())
    }

    private fun getIntent(): Intent {
        val intent = Intent(context, service)
        input?.let {
            intent.putExtras(it)
        }
        return intent
    }


}
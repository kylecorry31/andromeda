package com.kylecorry.andromeda.core.system

import android.content.Context

object CurrentApp {

    fun restart(context: Context) {
        val intent = Intents.restart(context)
        context.startActivity(intent)
        kill()
    }

    fun kill() {
        Runtime.getRuntime().exit(0)
    }

}
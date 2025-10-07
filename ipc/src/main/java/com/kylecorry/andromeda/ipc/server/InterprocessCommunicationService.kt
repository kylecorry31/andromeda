package com.kylecorry.andromeda.ipc.server

import android.app.Service
import android.content.Intent
import android.os.IBinder

abstract class InterprocessCommunicationService : Service() {
    abstract val router: InterprocessCommunicationRouter

    override fun onBind(intent: Intent?): IBinder? {
        return router.bind(this)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        router.unbind()
        return super.onUnbind(intent)
    }
}
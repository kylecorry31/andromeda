package com.kylecorry.andromeda.torch

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.topics.generic.BaseTopic
import com.kylecorry.andromeda.core.topics.generic.Topic
import com.kylecorry.andromeda.core.tryOrLog

class TorchStateChangedTopic(private val context: Context) : BaseTopic<Boolean>() {

    private val handler = Handler(Looper.getMainLooper())

    private val callback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            super.onTorchModeChanged(cameraId, enabled)
            topic.publish(enabled)
        }
    }

    override val topic: Topic<Boolean> = Topic.lazy(::register, ::unregister)

    private fun register() {
        tryOrLog {
            context.getSystemService<CameraManager>()?.registerTorchCallback(callback, handler)
        }
    }

    private fun unregister() {
        tryOrLog {
            context.getSystemService<CameraManager>()?.unregisterTorchCallback(callback)
        }
    }

}
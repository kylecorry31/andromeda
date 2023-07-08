package com.kylecorry.andromeda.core.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.kylecorry.andromeda.core.topics.generic.BaseTopic
import com.kylecorry.andromeda.core.topics.generic.Topic
import com.kylecorry.andromeda.core.tryOrLog

class BroadcastReceiverTopic(
    private val context: Context,
    private val intentFilter: IntentFilter,
    private val listenToBroadcastsFromOtherApps: Boolean = false
) :
    BaseTopic<Intent>() {

    override val topic: Topic<Intent> = Topic.lazy(this::start, this::stop)

    private fun start() {
        val flags = if (listenToBroadcastsFromOtherApps) {
            ContextCompat.RECEIVER_EXPORTED
        } else {
            ContextCompat.RECEIVER_NOT_EXPORTED
        }
        ContextCompat.registerReceiver(context, receiver, intentFilter, flags)
    }

    private fun stop() {
        tryOrLog {
            context.unregisterReceiver(receiver)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            topic.publish(intent)
        }
    }
}
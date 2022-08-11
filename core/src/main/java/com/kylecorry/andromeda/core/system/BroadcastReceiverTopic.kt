package com.kylecorry.andromeda.core.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.kylecorry.andromeda.core.topics.generic.BaseTopic
import com.kylecorry.andromeda.core.topics.generic.Topic
import com.kylecorry.andromeda.core.tryOrLog

class BroadcastReceiverTopic(private val context: Context, private val intentFilter: IntentFilter) :
    BaseTopic<Intent>() {

    override val topic: Topic<Intent> = Topic.lazy(this::start, this::stop)

    private fun start() {
        context.registerReceiver(receiver, intentFilter)
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
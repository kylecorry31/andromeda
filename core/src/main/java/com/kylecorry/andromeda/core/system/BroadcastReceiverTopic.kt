package com.kylecorry.andromeda.core.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.kylecorry.andromeda.core.topics.generic.BaseTopic
import com.kylecorry.andromeda.core.topics.generic.Topic

class BroadcastReceiverTopic(private val context: Context, private val intentFilter: IntentFilter) :
    BaseTopic<Intent>() {

    override val topic: Topic<Intent> = Topic.lazy(
        { context.registerReceiver(receiver, intentFilter) },
        { context.unregisterReceiver(receiver) },
    )

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            topic.notifySubscribers(intent)
        }
    }
}
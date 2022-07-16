package com.kylecorry.andromeda.core.sensors

import com.kylecorry.andromeda.core.topics.Subscriber
import com.kylecorry.andromeda.core.topics.Topic

abstract class AbstractSensor : ISensor {

    override val quality = Quality.Unknown

    private val topic = Topic(
        onSubscriberAdded = { count, _ -> if (count == 1) startImpl() },
        onSubscriberRemoved = { count, _ -> if (count == 0) stopImpl() }
    )

    override fun start(subscriber: Subscriber) {
        subscribe(subscriber)
    }

    override fun stop(subscriber: Subscriber?) {
        if (subscriber == null) {
            unsubscribeAll()
        } else {
            unsubscribe(subscriber)
        }
    }

    override suspend fun read() {
        topic.read()
    }

    override fun subscribe(subscriber: Subscriber) {
        topic.subscribe(subscriber)
    }

    override fun unsubscribe(subscriber: Subscriber) {
        topic.unsubscribe(subscriber)
    }

    override fun unsubscribeAll() {
        topic.unsubscribeAll()
    }

    protected abstract fun startImpl()
    protected abstract fun stopImpl()

    protected fun notifyListeners() {
        topic.notifySubscribers()
    }

}
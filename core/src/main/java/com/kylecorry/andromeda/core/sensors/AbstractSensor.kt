package com.kylecorry.andromeda.core.sensors

import com.kylecorry.andromeda.core.topics.BaseTopic
import com.kylecorry.andromeda.core.topics.Subscriber
import com.kylecorry.andromeda.core.topics.Topic

abstract class AbstractSensor : BaseTopic(), ISensor {

    override val quality = Quality.Unknown

    override val topic = Topic.lazy(::startImpl, ::stopImpl)

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

    protected abstract fun startImpl()
    protected abstract fun stopImpl()

    protected fun notifyListeners() {
        topic.publish()
    }

}
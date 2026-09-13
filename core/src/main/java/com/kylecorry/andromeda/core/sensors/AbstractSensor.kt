package com.kylecorry.andromeda.core.sensors

import android.os.SystemClock
import com.kylecorry.luna.topics.BaseTopic
import com.kylecorry.luna.topics.Subscriber
import com.kylecorry.luna.topics.Topic
import java.time.Instant

abstract class AbstractSensor : BaseTopic(), ISensor {

    override val quality = Quality.Unknown

    override var eventTimeElapsedNanos: Long = 0L
        protected set

    override var eventTime: Instant = Instant.EPOCH
        protected set

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

    protected fun setEventTimeToNow() {
        eventTimeElapsedNanos = SystemClock.elapsedRealtimeNanos()
        eventTime = Instant.now()
    }

    protected fun setEventTimeFrom(sensor: ISensor) {
        eventTimeElapsedNanos = sensor.eventTimeElapsedNanos
        eventTime = sensor.eventTime
    }

}
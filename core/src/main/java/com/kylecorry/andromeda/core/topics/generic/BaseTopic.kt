package com.kylecorry.andromeda.core.topics.generic

abstract class BaseTopic<T> : ITopic<T> {

    protected abstract val topic: ITopic<T>

    override fun subscribe(subscriber: Subscriber<T>) {
        topic.subscribe(subscriber)
    }

    override fun unsubscribe(subscriber: Subscriber<T>) {
        topic.unsubscribe(subscriber)
    }

    override fun unsubscribeAll() {
        topic.unsubscribeAll()
    }

    override suspend fun read(): T {
        return topic.read()
    }
}
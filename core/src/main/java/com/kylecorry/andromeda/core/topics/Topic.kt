package com.kylecorry.andromeda.core.topics

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class Topic(
    private val onSubscriberAdded: (count: Int, subscriber: Subscriber) -> Unit = { _, _ -> },
    private val onSubscriberRemoved: (count: Int, subscriber: Subscriber) -> Unit = { _, _ -> },
) : ITopic {

    private val subscribers = mutableSetOf<Subscriber>()

    override fun subscribe(subscriber: Subscriber) {
        synchronized(subscribers) {
            val wasAdded = subscribers.add(subscriber)
            if (wasAdded) {
                onSubscriberAdded(subscribers.size, subscriber)
            }
        }
    }

    override fun unsubscribe(subscriber: Subscriber) {
        synchronized(subscribers) {
            val wasRemoved = subscribers.remove(subscriber)
            if (wasRemoved) {
                onSubscriberRemoved(subscribers.size, subscriber)
            }
        }
    }

    override fun unsubscribeAll() {
        subscribers.map { it }.forEach(::unsubscribe)
    }

    override suspend fun read() = suspendCancellableCoroutine<Unit> { cont ->
        val callback: () -> Boolean = {
            cont.resume(Unit)
            false
        }
        cont.invokeOnCancellation {
            unsubscribe(callback)
        }
        subscribe(callback)
    }

    fun publish() {
        synchronized(subscribers) {
            val finishedListeners = subscribers.filter { !it.invoke() }
            finishedListeners.forEach(::unsubscribe)
        }
    }

    companion object {

        /**
         * Creates a topic that will start when one subscriber is added and stop when none are left
         */
        fun lazy(start: () -> Unit, stop: () -> Unit): Topic {
            return Topic(
                { count, _ -> if (count == 1) start() },
                { count, _ -> if (count == 0) stop() }
            )
        }
    }
}
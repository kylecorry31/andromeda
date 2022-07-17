package com.kylecorry.andromeda.core.topics.generic

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class Topic<T>(
    private val onSubscriberAdded: (count: Int, subscriber: Subscriber<T>) -> Unit = { _, _ -> },
    private val onSubscriberRemoved: (count: Int, subscriber: Subscriber<T>) -> Unit = { _, _ -> },
) : ITopic<T> {

    private val subscribers = mutableSetOf<Subscriber<T>>()

    override fun subscribe(subscriber: Subscriber<T>) {
        synchronized(subscribers) {
            subscribers.add(subscriber)
            onSubscriberAdded(subscribers.size, subscriber)
        }
    }

    override fun unsubscribe(subscriber: Subscriber<T>) {
        synchronized(subscribers) {
            subscribers.remove(subscriber)
            onSubscriberRemoved(subscribers.size, subscriber)
        }
    }

    override fun unsubscribeAll() {
        synchronized(subscribers) {
            subscribers.map { it }.forEach(::unsubscribe)
        }
    }

    override suspend fun read(): T = suspendCancellableCoroutine { cont ->
        val callback: (T) -> Boolean = {
            cont.resume(it)
            false
        }
        cont.invokeOnCancellation {
            unsubscribe(callback)
        }
        subscribe(callback)
    }

    fun notifySubscribers(value: T) {
        synchronized(subscribers) {
            val finishedListeners = subscribers.filter { !it.invoke(value) }
            finishedListeners.forEach(::unsubscribe)
        }
    }

    companion object {

        /**
         * Creates a topic that will start when one subscriber is added and stop when none are left
         */
        fun <T> lazy(start: () -> Unit, stop: () -> Unit): Topic<T> {
            return Topic(
                { count, _ -> if (count == 1) start() },
                { count, _ -> if (count == 0) stop() }
            )
        }
    }

}
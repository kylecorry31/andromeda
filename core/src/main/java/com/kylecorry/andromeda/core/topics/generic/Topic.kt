package com.kylecorry.andromeda.core.topics.generic

import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume

class Topic<T>(
    private val onSubscriberAdded: (count: Int, subscriber: Subscriber<T>) -> Unit = { _, _ -> },
    private val onSubscriberRemoved: (count: Int, subscriber: Subscriber<T>) -> Unit = { _, _ -> },
    defaultValue: Optional<T> = Optional.empty()
) : ITopic<T> {

    override var value: Optional<T> = defaultValue
        private set

    private val subscribers = mutableSetOf<Subscriber<T>>()

    override fun subscribe(subscriber: Subscriber<T>) {
        synchronized(subscribers) {
            val wasAdded = subscribers.add(subscriber)
            if (wasAdded) {
                onSubscriberAdded(subscribers.size, subscriber)
            }
        }
    }

    override fun unsubscribe(subscriber: Subscriber<T>) {
        synchronized(subscribers) {
            val wasRemoved = subscribers.remove(subscriber)
            if (wasRemoved) {
                onSubscriberRemoved(subscribers.size, subscriber)
            }
        }
    }

    override fun unsubscribeAll() {
        val copy = synchronized(subscribers) { subscribers.toList() }
        copy.forEach(::unsubscribe)
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

    fun publish(value: T) {
        this.value = Optional.of(value)
        val subs = synchronized(subscribers) {
            subscribers.toList()
        }
        subs.filter { !it.invoke(value) }.forEach(::unsubscribe)
    }

    companion object {

        /**
         * Creates a topic that will start when one subscriber is added and stop when none are left
         */
        fun <T> lazy(
            start: () -> Unit,
            stop: () -> Unit,
            defaultValue: Optional<T> = Optional.empty()
        ): Topic<T> {
            return Topic(
                { count, _ -> if (count == 1) start() },
                { count, _ -> if (count == 0) stop() },
                defaultValue
            )
        }
    }
}
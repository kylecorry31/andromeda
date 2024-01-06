package com.kylecorry.andromeda.core.topics.generic

import java.util.*

class TopicOperator<V: Any, T: Any>(
    private val baseTopic: ITopic<V>,
    initialValue: Optional<T> = Optional.empty(),
    private val onSubscriberAdded: (count: Int, subscriber: Subscriber<T>, result: Topic<T>) -> Unit = { _, _, _ -> },
    private val onSubscriberRemoved: (count: Int, subscriber: Subscriber<T>, result: Topic<T>) -> Unit = { _, _, _ -> },
    private val onValue: (Topic<T>, ITopic<V>, V) -> Unit,
) :
    BaseTopic<T>() {

    override val topic = Topic(
        { count, subscriber ->
            onSubscriberAdded(count, subscriber)
        },
        { count, subscriber ->
            onSubscriberRemoved(count, subscriber)
        },
        initialValue
    )

    private fun onSubscriberAdded(count: Int, subscriber: Subscriber<T>){
        onSubscriberAdded(count, subscriber, topic)
        if (count == 1) baseTopic.subscribe(this::onValue)
    }

    private fun onSubscriberRemoved(count: Int, subscriber: Subscriber<T>){
        onSubscriberRemoved(count, subscriber, topic)
        if (count == 0) baseTopic.unsubscribe(this::onValue)
    }

    private fun onValue(value: V): Boolean {
        onValue(topic, baseTopic, value)
        return true
    }
}
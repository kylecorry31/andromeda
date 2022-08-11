package com.kylecorry.andromeda.core.topics.generic

internal class MappedTopic<T, V>(private val baseTopic: ITopic<T>, private val map: (T) -> V) :
    BaseTopic<V>() {

    override val topic = Topic.lazy<V>(
        { baseTopic.subscribe(this::onValue) },
        { baseTopic.unsubscribe(this::onValue) },
        baseTopic.value.map { map(it) }
    )

    private fun onValue(value: T): Boolean {
        topic.publish(map(value))
        return true
    }
}
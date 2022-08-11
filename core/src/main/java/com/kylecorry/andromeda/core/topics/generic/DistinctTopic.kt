package com.kylecorry.andromeda.core.topics.generic

internal class DistinctTopic<T>(private val baseTopic: ITopic<T>) : BaseTopic<T>() {

    override val topic = Topic.lazy(
        { baseTopic.subscribe(this::onValue) },
        { baseTopic.unsubscribe(this::onValue) },
        baseTopic.value
    )

    private fun onValue(value: T): Boolean {
        val current = topic.value
        if (current.isEmpty || current.get() != value) {
            topic.publish(value)
        }
        return true
    }
}
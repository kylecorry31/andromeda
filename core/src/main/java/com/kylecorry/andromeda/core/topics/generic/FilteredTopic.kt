package com.kylecorry.andromeda.core.topics.generic

internal class FilteredTopic<T>(private val baseTopic: ITopic<T>, private val predicate: (T) -> Boolean) : BaseTopic<T>() {

    override val topic = Topic.lazy(
        { baseTopic.subscribe(this::onValue) },
        { baseTopic.unsubscribe(this::onValue) },
        baseTopic.value.filter(predicate)
    )

    private fun onValue(value: T): Boolean {
        if (predicate(value)) {
            topic.notifySubscribers(value)
        }
        return true
    }
}
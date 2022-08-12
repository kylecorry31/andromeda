package com.kylecorry.andromeda.core.topics.generic

import java.util.*

internal class LazyValueTopic<T>(
    private val baseTopic: ITopic<T>,
    private val onlyOnStart: Boolean = false,
    private val valueProvider: () -> Optional<T>
) : BaseTopic<T>() {

    private var _value = baseTopic.value

    private var isRunning = false

    override val value: Optional<T>
        get() {
            return if (onlyOnStart || isRunning) {
                _value
            } else {
                valueProvider()
            }
        }

    override val topic = Topic.lazy<T>(
        {
            _value = valueProvider()
            isRunning = true
            baseTopic.subscribe(this::onValue)
        },
        {
            isRunning = false
            baseTopic.unsubscribe(this::onValue)
        }
    )

    private fun onValue(value: T): Boolean {
        _value = Optional.of(value)
        topic.publish(value)
        return true
    }
}
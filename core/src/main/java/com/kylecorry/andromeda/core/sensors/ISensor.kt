package com.kylecorry.andromeda.core.sensors

import com.kylecorry.andromeda.core.topics.ITopic
import com.kylecorry.andromeda.core.topics.Subscriber

interface ISensor : ITopic {

    val quality: Quality

    val hasValidReading: Boolean

    fun start(subscriber: Subscriber)

    fun stop(subscriber: Subscriber?)
}
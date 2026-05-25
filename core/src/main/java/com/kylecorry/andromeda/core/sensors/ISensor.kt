package com.kylecorry.andromeda.core.sensors

import com.kylecorry.luna.topics.ITopic
import com.kylecorry.luna.topics.Subscriber

interface ISensor : ITopic {

    val quality: Quality

    val hasValidReading: Boolean

    fun start(subscriber: Subscriber)

    fun stop(subscriber: Subscriber?)
}
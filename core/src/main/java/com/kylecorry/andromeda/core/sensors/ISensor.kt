package com.kylecorry.andromeda.core.sensors

import com.kylecorry.luna.topics.ITopic
import com.kylecorry.luna.topics.Subscriber
import java.time.Instant

interface ISensor : ITopic {

    val quality: Quality

    val hasValidReading: Boolean

    /**
     * The time of the latest reading, in nanoseconds. 0 if there hasn't been a reading.
     */
    val eventTimeElapsedNanos: Long

    /**
     * The wall clock time of the latest reading, or Instant.EPOCH if there hasn't been a reading
     */
    val eventTime: Instant

    fun start(subscriber: Subscriber)

    fun stop(subscriber: Subscriber?)
}

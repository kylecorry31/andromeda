package com.kylecorry.andromeda.core.topics.generic

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class TopicTest {

    @Test
    fun canPublish() {
        var value: Int? = null
        val subscriber: (Int) -> Boolean = {
            value = it
            true
        }

        val topic = Topic<Int>()

        topic.subscribe(subscriber)

        topic.publish(1)
        assertEquals(1, value)
        assertEquals(1, topic.value.get())

        topic.publish(2)
        assertEquals(2, value)
        assertEquals(2, topic.value.get())
    }

    @Test
    fun canHaveDefaultValue() {
        val topic = Topic(defaultValue = Optional.of(1))
        assertEquals(1, topic.value.get())
    }

    @Test
    fun canMap() {
        var value: Int? = null
        val subscriber: (Int) -> Boolean = {
            value = it
            true
        }

        val topic = Topic<Int>()
        val mapped = topic.map { it * 2 }

        mapped.subscribe(subscriber)

        topic.publish(1)
        assertEquals(2, value)

        topic.publish(2)
        assertEquals(4, value)
    }

    @Test
    fun canSuspendMap() = runBlocking {
        var value: Int? = null
        val subscriber: (Int) -> Boolean = {
            value = it
            true
        }

        val topic = Topic<Int>()
        val mapped = topic.suspendMap { it * 2 }

        mapped.subscribe(subscriber)

        topic.publish(1)
        delay(20)
        assertEquals(2, value)

        topic.publish(2)
        delay(20)
        assertEquals(4, value)
    }

    @Test
    fun canTap() {
        var value: Int? = null
        val subscriber: (Int) -> Boolean = {
            true
        }

        val topic = Topic<Int>()
        val mapped = topic.tap { value = 2 }

        mapped.subscribe(subscriber)

        topic.publish(1)
        assertEquals(2, value)

        topic.publish(2)
        assertEquals(2, value)
    }

    @Test
    fun canFilter() {
        var value: Int? = null
        val subscriber: (Int) -> Boolean = {
            value = it
            true
        }

        val topic = Topic<Int>()
        val mapped = topic.filter { it % 2 == 0 }

        mapped.subscribe(subscriber)

        topic.publish(1)
        assertEquals(null, value)

        topic.publish(2)
        assertEquals(2, value)
    }

    @Test
    fun canDistinct() {
        var value: Int? = null
        var count = 0
        val subscriber: (Int) -> Boolean = {
            value = it
            count++
            true
        }

        val topic = Topic<Int>()
        val mapped = topic.distinct()

        mapped.subscribe(subscriber)

        topic.publish(1)
        topic.publish(1)
        topic.publish(1)
        assertEquals(1, value)
        assertEquals(1, count)

        topic.publish(2)
        topic.publish(2)
        assertEquals(2, value)
        assertEquals(2, count)

        topic.publish(1)
        assertEquals(1, value)
        assertEquals(3, count)

    }


    @Test
    fun canReplay() {
        var value: Int? = null
        val subscriber: (Int) -> Boolean = {
            value = it
            true
        }

        val topic = Topic<Int>()
        val mapped = topic.replay()

        mapped.subscribe { true }
        topic.publish(1)

        mapped.subscribe(subscriber)
        assertEquals(1, value)

        topic.publish(2)
        assertEquals(2, value)
    }

    @Test
    fun canCollect() {
        var value: List<Int>? = null
        val subscriber: (List<Int>) -> Boolean = {
            value = it
            true
        }

        val topic = Topic<Int>()
        val mapped = topic.collect(2, 5)

        mapped.subscribe(subscriber)

        topic.publish(1)
        assertEquals(null, value)

        topic.publish(2)
        assertEquals(listOf(1, 2), value)

        topic.publish(3)
        assertEquals(listOf(1, 2, 3), value)

        topic.publish(4)
        assertEquals(listOf(1, 2, 3, 4), value)

        topic.publish(5)
        assertEquals(listOf(1, 2, 3, 4, 5), value)

        topic.publish(6)
        assertEquals(listOf(2, 3, 4, 5, 6), value)
    }

    @Test
    fun canChainOperators() {
        var value: Int? = null
        var count = 0
        val subscriber: (Int) -> Boolean = {
            value = it
            count++
            true
        }

        val topic = Topic<Int>()
        val mapped = topic
            .map {
                it * 2
            }
            .filter {
                it % 3 == 0
            }
            .distinct()

        mapped.subscribe(subscriber)

        topic.publish(1)
        assertEquals(0, count)
        assertEquals(null, value)

        topic.publish(3)
        assertEquals(1, count)
        assertEquals(6, value)

        topic.publish(3)
        assertEquals(1, count)
        assertEquals(6, value)

        topic.publish(5)
        assertEquals(1, count)
        assertEquals(6, value)

        topic.publish(6)
        assertEquals(2, count)
        assertEquals(12, value)

        topic.publish(3)
        assertEquals(3, count)
        assertEquals(6, value)
    }

}
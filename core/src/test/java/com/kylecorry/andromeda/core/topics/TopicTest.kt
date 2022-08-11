package com.kylecorry.andromeda.core.topics

import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TopicTest {

    @Test
    fun canPublish() {
        val wasCalled = mutableListOf(false, false)
        val subscriber1: () -> Boolean = {
            wasCalled[0] = true
            true
        }

        val subscriber2: () -> Boolean = {
            wasCalled[1] = true
            true
        }

        val topic = Topic(
            { c, s ->
                if (c == 1) {
                    assertEquals(subscriber1, s)
                } else {
                    assertEquals(subscriber2, s)
                }
            }
        )


        topic.subscribe(subscriber1)
        topic.subscribe(subscriber2)

        assertFalse(wasCalled[0])
        assertFalse(wasCalled[1])

        topic.publish()

        assertTrue(wasCalled[0])
        assertTrue(wasCalled[1])
    }

    @Test
    fun canUnsubscribe() {
        val wasCalled = mutableListOf(false, false)
        val subscriber1: () -> Boolean = {
            wasCalled[0] = true
            true
        }

        val subscriber2: () -> Boolean = {
            wasCalled[1] = true
            true
        }

        var called = 0

        val topic = Topic(
            { c, s ->
                if (c == 1) {
                    assertEquals(subscriber1, s)
                } else {
                    assertEquals(subscriber2, s)
                }
            },
            { c, s ->
                called++
                assertEquals(subscriber2, s)
            }
        )

        topic.subscribe(subscriber1)
        topic.subscribe(subscriber2)

        topic.unsubscribe(subscriber2)

        // Unsubscribe a second time (this should do nothing)
        topic.unsubscribe(subscriber2)

        assertFalse(wasCalled[0])
        assertFalse(wasCalled[1])

        topic.publish()

        assertTrue(wasCalled[0])
        assertFalse(wasCalled[1])
        assertEquals(1, called)
    }

    @Test
    fun canUnsubscribeAll() {
        val wasCalled = mutableListOf(false, false)
        val subscriber1: () -> Boolean = {
            wasCalled[0] = true
            true
        }

        val subscriber2: () -> Boolean = {
            wasCalled[1] = true
            true
        }

        val topic = Topic(
            { c, s ->
                if (c == 1) {
                    assertEquals(subscriber1, s)
                } else {
                    assertEquals(subscriber2, s)
                }
            },
            { c, s ->
                if (c == 1) {
                    assertEquals(subscriber1, s)
                } else {
                    assertEquals(subscriber2, s)
                }
            }
        )

        topic.subscribe(subscriber1)
        topic.subscribe(subscriber2)

        topic.unsubscribeAll()

        assertFalse(wasCalled[0])
        assertFalse(wasCalled[1])

        topic.publish()

        assertFalse(wasCalled[0])
        assertFalse(wasCalled[1])
    }

    @Test
    fun canUnsubscribeWithReturnValue() {
        val topic = Topic()
        val wasCalled = mutableListOf(false, false)
        val subscriber1: () -> Boolean = {
            wasCalled[0] = true
            true
        }

        val subscriber2: () -> Boolean = {
            wasCalled[1] = true
            false
        }
        topic.subscribe(subscriber1)
        topic.subscribe(subscriber2)

        topic.publish()

        wasCalled[1] = false

        topic.publish()

        assertTrue(wasCalled[0])
        assertFalse(wasCalled[1])
    }

    @Test
    fun canRead() = runBlocking {
        val counts = mutableListOf<Int>()
        val topic = Topic(
            { c, _ ->
                counts.add(c)
            },
            { c, _ ->
                counts.add(c)
            }
        )
        val jobs = listOf(
            launch { topic.read() },
            launch { topic.publish() }
        )
        jobs.joinAll()

        assertEquals(listOf(1, 0), counts)
    }

    @Test
    fun canLazilySubscribe(){
        var subscribed = false
        val subscriber1 = { true }

        val subscriber2 = { true }

        val topic = Topic.lazy({ subscribed = true }, { subscribed = false })

        assertFalse(subscribed)

        topic.subscribe(subscriber1)

        assertTrue(subscribed)

        topic.subscribe(subscriber2)

        assertTrue(subscribed)

        topic.unsubscribe(subscriber2)

        assertTrue(subscribed)

        topic.unsubscribe(subscriber1)

        assertFalse(subscribed)
    }

}
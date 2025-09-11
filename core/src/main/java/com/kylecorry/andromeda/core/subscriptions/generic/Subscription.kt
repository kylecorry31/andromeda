package com.kylecorry.andromeda.core.subscriptions.generic

import com.kylecorry.andromeda.core.annotations.ExperimentalUsage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@ExperimentalUsage("Unstable API")
class Subscription<T>(
    replay: Int = 0,
    bufferSize: Int = 1,
    bufferOverflowBehavior: BufferOverflow = BufferOverflow.DROP_OLDEST,
    private val onStart: suspend () -> Unit = {},
    private val onStop: suspend () -> Unit = {},
) : ISubscription<T> {

    private val startStopLock = Mutex()
    private var activeListeners = 0
    private val sharedFlow = MutableSharedFlow<T>(
        replay = replay,
        extraBufferCapacity = bufferSize,
        onBufferOverflow = bufferOverflowBehavior
    )

    private val subscriptionFlow = sharedFlow
        .onStart { startSubscription() }
        .onCompletion { stopSubscription() }

    private val scope = CoroutineScope(Dispatchers.Default)
    private val jobLock = Any()
    private var listeners: MutableMap<suspend (T) -> Unit, Job> = mutableMapOf()

    override fun subscribe(listener: suspend (value: T) -> Unit) {
        subscribe(listener) { it }
    }

    override fun subscribe(
        listener: suspend (value: T) -> Unit,
        modifiers: (Flow<T>) -> Flow<T>
    ) {
        synchronized(jobLock) {
            listeners[listener]?.cancel()
            listeners[listener] = scope.launch {
                modifiers(subscriptionFlow).collect { listener(it) }
            }
        }
    }

    override fun unsubscribe(listener: suspend (value: T) -> Unit) {
        synchronized(jobLock) {
            listeners.remove(listener)?.cancel()
        }
    }

    override fun unsubscribeAll() {
        synchronized(jobLock) {
            listeners.values.forEach { it.cancel() }
            listeners.clear()
        }
    }

    override fun publish(value: T) {
        sharedFlow.tryEmit(value)
    }

    override fun flow(): Flow<T> = subscriptionFlow

    private suspend fun startSubscription() {
        startStopLock.withLock {
            if (activeListeners == 0) {
                onStart()
            }
            activeListeners++
        }
    }

    private suspend fun stopSubscription() {
        startStopLock.withLock {
            activeListeners--
            if (activeListeners == 0) {
                onStop()
            }
        }
    }
}
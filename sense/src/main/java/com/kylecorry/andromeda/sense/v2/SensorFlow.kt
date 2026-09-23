package com.kylecorry.andromeda.sense.v2

import com.kylecorry.luna.concurrency.IFlowable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

abstract class SensorFlow<T : ISensorReading>(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
    stopTimeoutMillis: Long = 0
) : IFlowable<T> {
    private val mutableLastKnown = MutableStateFlow<T?>(null)
    val lastKnown: StateFlow<T?> = mutableLastKnown.asStateFlow()
    private val mutableIsActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = mutableIsActive.asStateFlow()

    override val flow: SharedFlow<T> = callbackFlow {
        setupListener()
    }
        .onEach { mutableLastKnown.value = it }
        .conflate()
        .shareIn(scope, started = SharingStarted.WhileSubscribed(stopTimeoutMillis), 0)

    protected abstract suspend fun ProducerScope<T>.setupListener()
    protected fun setLastKnownValue(value: T?) {
        mutableLastKnown.value = value
    }

    protected fun setIsActive(value: Boolean) {
        mutableIsActive.value = value
    }
}

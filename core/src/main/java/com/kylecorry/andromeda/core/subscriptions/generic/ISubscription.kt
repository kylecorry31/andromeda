package com.kylecorry.andromeda.core.subscriptions.generic

import com.kylecorry.andromeda.core.annotations.ExperimentalUsage
import kotlinx.coroutines.flow.Flow

@ExperimentalUsage("Unstable API")
interface ISubscription<T> {
    fun subscribe(listener: suspend (T) -> Unit)

    fun subscribe(listener: suspend (T) -> Unit, modifiers: (flow: Flow<T>) -> Flow<T>)

    fun unsubscribe(listener: suspend (T) -> Unit)

    fun unsubscribeAll()

    fun publish(value: T)

    fun flow(): Flow<T>
}
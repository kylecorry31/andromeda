package com.kylecorry.andromeda.core.subscriptions

import com.kylecorry.andromeda.core.annotations.ExperimentalUsage
import kotlinx.coroutines.flow.Flow

@ExperimentalUsage("Unstable API")
interface ISubscription {
    fun subscribe(listener: suspend () -> Unit)

    fun subscribe(listener: suspend () -> Unit, modifiers: (flow: Flow<Unit>) -> Flow<Unit>)

    fun unsubscribe(listener: suspend () -> Unit)

    fun unsubscribeAll()

    fun publish()

    fun flow(): Flow<Unit>
}
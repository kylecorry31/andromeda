package com.kylecorry.andromeda.core.coroutines

import kotlinx.coroutines.*

suspend inline fun <T> makeSuspend(crossinline action: (TypedContinuationWrapper<T>) -> Unit): T =
    suspendCancellableCoroutine {
        action(TypedContinuationWrapper(it))
    }

suspend inline fun makeSuspend(crossinline action: (ContinuationWrapper) -> Unit) =
    suspendCancellableCoroutine {
        action(ContinuationWrapper(it))
    }

suspend fun <T> onMain(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.Main, block)

suspend fun <T> onDefault(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.Default, block)

suspend fun <T> onIO(block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.IO, block)

enum class BackgroundMinimumState {
    Resumed,
    Started,
    Created,
    Any
}
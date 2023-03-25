package com.kylecorry.andromeda.core.coroutines

import kotlinx.coroutines.*

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
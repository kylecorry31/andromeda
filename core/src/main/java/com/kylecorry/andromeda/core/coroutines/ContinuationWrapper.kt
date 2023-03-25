package com.kylecorry.andromeda.core.coroutines

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ContinuationWrapper(private val continuation: CancellableContinuation<Unit>) {
    fun resume() {
        continuation.resume(Unit)
    }

    fun resumeWithException(exception: Throwable) {
        continuation.resumeWithException(exception)
    }

    fun cancel() {
        continuation.cancel()
    }

    fun setOnCancelListener(listener: () -> Unit) {
        continuation.invokeOnCancellation {
            listener()
        }
    }
}
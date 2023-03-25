package com.kylecorry.andromeda.core.coroutines

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TypedContinuationWrapper<T>(private val continuation: CancellableContinuation<T>) {
    fun resume(value: T) {
        continuation.resume(value)
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
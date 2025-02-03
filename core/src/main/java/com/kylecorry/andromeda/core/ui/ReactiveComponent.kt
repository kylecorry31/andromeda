package com.kylecorry.andromeda.core.ui

import android.content.Context
import com.kylecorry.andromeda.core.system.Resources

interface ReactiveComponent {
    fun useAndroidContext(): Context
    fun useEffect(vararg values: Any?, action: () -> Unit)
    fun useEffectWithCleanup(vararg values: Any?, action: () -> () -> Unit)
    fun <T> useMemo(vararg values: Any?, value: () -> T): T
    fun <T> useState(initialValue: T): Pair<T, (T) -> Unit>
}

fun <T> ReactiveComponent.useCallback(vararg values: Any?, callback: () -> T): () -> T {
    return useMemo(*values) { callback }
}

fun <T, R> ReactiveComponent.useCallback(vararg values: Any?, callback: (R) -> T): (R) -> T {
    return useMemo(*values) { callback }
}

fun <T, R, S> ReactiveComponent.useCallback(
    vararg values: Any?,
    callback: (R, S) -> T
): (R, S) -> T {
    return useMemo(*values) { callback }
}

fun <T, R, S, U> ReactiveComponent.useCallback(
    vararg values: Any?,
    callback: (R, S, U) -> T
): (R, S, U) -> T {
    return useMemo(*values) { callback }
}

fun <T, R, S, U, V> ReactiveComponent.useCallback(
    vararg values: Any?,
    callback: (R, S, U, V) -> T
): (R, S, U, V) -> T {
    return useMemo(*values) { callback }
}

fun <T, R, S, U, V, W> ReactiveComponent.useCallback(
    vararg values: Any?,
    callback: (R, S, U, V, W) -> T
): (R, S, U, V, W) -> T {
    return useMemo(*values) { callback }
}

fun ReactiveComponent.useString(resId: Int): String {
    val context = useAndroidContext()
    return useMemo(context, resId) {
        context.getString(resId)
    }
}

fun ReactiveComponent.useString(resId: Int, vararg formatArgs: Any): String {
    val context = useAndroidContext()
    return useMemo(context, resId, formatArgs) {
        context.getString(resId, *formatArgs)
    }
}

fun ReactiveComponent.useSizeDp(dp: Float): Float {
    val context = useAndroidContext()
    return useMemo(context, dp) {
        Resources.dp(context, dp)
    }
}

fun ReactiveComponent.useSizeSp(sp: Float): Float {
    val context = useAndroidContext()
    return useMemo(context, sp) {
        Resources.sp(context, sp)
    }
}
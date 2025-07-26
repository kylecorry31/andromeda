package com.kylecorry.andromeda.core.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.lifecycle.LifecycleOwner
import com.kylecorry.andromeda.core.cache.AppServiceRegistry
import com.kylecorry.andromeda.core.system.Resources
import com.kylecorry.luna.hooks.Ref

interface ReactiveComponent {
    fun useAndroidContext(): Context
    fun useEffect(vararg values: Any?, action: () -> Unit)
    fun useEffectWithCleanup(vararg values: Any?, action: () -> () -> Unit)
    fun <T> useMemo(vararg values: Any?, value: () -> T): T
    fun <T> useState(initialValue: T): Pair<T, (T) -> Unit>
    fun <T> useView(@IdRes id: Int): T
    fun <T> useRef(initialValue: T): Ref<T>
    fun useRootView(): View
    fun useLifecycleOwner(viewOwner: Boolean = true): LifecycleOwner
    fun useArguments(): Bundle
    fun useActivity(): Activity
}

fun <T> ReactiveComponent.useCallback(vararg values: Any?, callback: () -> T): () -> T {
    return useMemo(*values) { callback }
}

fun <R, T> ReactiveComponent.useCallback(vararg values: Any?, callback: (R) -> T): (R) -> T {
    return useMemo(*values) { callback }
}

fun <R, S, T> ReactiveComponent.useCallback(
    vararg values: Any?,
    callback: (R, S) -> T
): (R, S) -> T {
    return useMemo(*values) { callback }
}

fun <R, S, U, T> ReactiveComponent.useCallback(
    vararg values: Any?,
    callback: (R, S, U) -> T
): (R, S, U) -> T {
    return useMemo(*values) { callback }
}

fun <R, S, U, V, T> ReactiveComponent.useCallback(
    vararg values: Any?,
    callback: (R, S, U, V) -> T
): (R, S, U, V) -> T {
    return useMemo(*values) { callback }
}

fun <R, S, U, V, W, T> ReactiveComponent.useCallback(
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

inline fun <reified T : Any> ReactiveComponent.useService(): T {
    return useMemo {
        AppServiceRegistry.get<T>()
    }
}
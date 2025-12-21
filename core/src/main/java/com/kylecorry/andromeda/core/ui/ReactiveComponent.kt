package com.kylecorry.andromeda.core.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.kylecorry.andromeda.core.cache.AppServiceRegistry
import com.kylecorry.andromeda.core.system.Resources
import com.kylecorry.luna.hooks.Ref
import com.kylecorry.luna.timer.CoroutineTimer
import com.kylecorry.luna.timer.TimerActionBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import kotlin.coroutines.CoroutineContext

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

fun ReactiveComponent.useLifecycleEffect(
    lifecycleEvent: Lifecycle.Event,
    vararg values: Any?,
    action: () -> Unit
) {
    val owner = useLifecycleOwner()
    val (lastObserver, setLastObserver) = useState<LifecycleObserver?>(null)
    val observer = useMemo(*values, lifecycleEvent) {
        LifecycleEventObserver { source: LifecycleOwner, event: Lifecycle.Event ->
            if (event == lifecycleEvent) {
                action()
            }
        }
    }

    useEffect(owner, observer) {
        setLastObserver(observer)
        lastObserver?.let {
            owner.lifecycle.removeObserver(it)
        }
        owner.lifecycle.addObserver(observer)
    }
}

fun ReactiveComponent.useDestroyEffect(vararg values: Any?, action: () -> Unit) {
    useLifecycleEffect(
        Lifecycle.Event.ON_DESTROY,
        *values
    ) {
        action()
    }
}

fun ReactiveComponent.usePauseEffect(vararg values: Any?, action: () -> Unit) {
    useLifecycleEffect(
        Lifecycle.Event.ON_PAUSE,
        *values
    ) {
        action()
    }
}

fun ReactiveComponent.useResumeEffect(vararg values: Any?, action: () -> Unit) {
    useLifecycleEffect(
        Lifecycle.Event.ON_RESUME,
        *values
    ) {
        action()
    }
}

fun <T : Any, V> ReactiveComponent.useLiveData(
    data: LiveData<T>,
    default: V,
    mapper: (T) -> V
): V {
    val (state, setState) = useState(default)
    val owner = useLifecycleOwner()

    // Note: This does not change when the mapper changes
    useEffect(data, owner) {
        data.observe(owner) {
            setState(mapper(it))
        }
    }

    return state
}

fun <T : Any, V> ReactiveComponent.useLiveData(
    data: LiveData<T>,
    mapper: (T) -> V?
): V? {
    return useLiveData(data, null, mapper)
}

fun ReactiveComponent.useTimer(
    interval: Long,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    observeOn: CoroutineContext = Dispatchers.Main,
    actionBehavior: TimerActionBehavior = TimerActionBehavior.Wait,
    runnable: suspend () -> Unit
) {
    val timer = useMemo {
        CoroutineTimer(scope, observeOn, actionBehavior, runnable)
    }

    useResumeEffect(timer, interval) {
        timer.interval(interval)
    }

    usePauseEffect(timer) {
        timer.stop()
    }
}

fun ReactiveComponent.useTrigger(): Pair<String, () -> Unit> {
    val (key, setKey) = useState("")
    val trigger = useCallback<Unit> {
        setKey(UUID.randomUUID().toString())
    }
    return useMemo(key, trigger) { key to trigger }
}
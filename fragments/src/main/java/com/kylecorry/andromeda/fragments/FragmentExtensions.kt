package com.kylecorry.andromeda.fragments

import android.app.Activity
import android.app.Application
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withStateAtLeast
import com.google.android.material.color.DynamicColors
import com.kylecorry.andromeda.core.coroutines.BackgroundMinimumState
import com.kylecorry.andromeda.core.topics.ITopic
import com.kylecorry.andromeda.core.ui.ReactiveComponent
import com.kylecorry.luna.hooks.Hooks
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

fun Fragment.switchToFragment(
    fragment: Fragment,
    @IdRes holderId: Int,
    addToBackStack: Boolean = false
) {
    parentFragmentManager.commit(true) {
        if (addToBackStack) {
            this.addToBackStack(null)
        }
        this.replace(
            holderId,
            fragment
        )
    }
}

fun DialogFragment.show(fragment: Fragment, tag: String = javaClass.name) {
    show(fragment.requireActivity(), tag)
}

fun DialogFragment.show(activity: FragmentActivity, tag: String = javaClass.name) {
    show(activity.supportFragmentManager, tag)
}

fun Fragment.onBackPressed(
    enabled: Boolean = true,
    onBackPressed: OnBackPressedCallback.() -> Unit
): OnBackPressedCallback {
    return requireActivity().onBackPressedDispatcher.addCallback(this, enabled, onBackPressed)
}

fun Activity.useDynamicColors() {
    DynamicColors.applyToActivityIfAvailable(this)
}

fun Application.useDynamicColors() {
    DynamicColors.applyToActivitiesIfAvailable(this)
}

inline fun LifecycleOwner.repeatInBackground(
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    val minimumState = when (state) {
        BackgroundMinimumState.Resumed -> Lifecycle.State.RESUMED
        BackgroundMinimumState.Started -> Lifecycle.State.STARTED
        BackgroundMinimumState.Created -> Lifecycle.State.CREATED
        BackgroundMinimumState.Any -> Lifecycle.State.CREATED
    }

    lifecycleScope.launch {
        repeatOnLifecycle(minimumState) {
            block()
        }
    }
}

fun LifecycleOwner.inBackground(
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val minimumState = when (state) {
        BackgroundMinimumState.Resumed -> Lifecycle.State.RESUMED
        BackgroundMinimumState.Started -> Lifecycle.State.STARTED
        BackgroundMinimumState.Created -> Lifecycle.State.CREATED
        BackgroundMinimumState.Any -> null
    }

    return lifecycleScope.launch {

        // If there is no minimum state, just run the block
        if (minimumState == null) {
            block()
            return@launch
        }


        if (!cancelWhenBelowState) {
            // The block will just run until completion once the state is reached
            waitUntilState(minimumState, throwOnCancel = false)
            block()
        } else {
            // The block will be cancelled if the state falls below the minimum state
            // Need to use repeat on lifecycle because there's no better way to do this anymore
            var isComplete = false
            repeatOnLifecycle(minimumState) {
                if (!isComplete) {
                    try {
                        block()
                    } finally {
                        isComplete = true
                        this.cancel()
                    }
                }
            }
        }
    }
}

suspend inline fun LifecycleOwner.waitUntilState(
    state: Lifecycle.State,
    throwOnCancel: Boolean = false,
    crossinline block: () -> Unit = {}
) {
    try {
        withStateAtLeast(state) {
            block()
        }
    } catch (e: CancellationException) {
        if (throwOnCancel) {
            throw e
        }
    }
}

fun LifecycleOwner.scheduleStateUpdates(hooks: Hooks) {
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            hooks.startStateUpdates()
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            hooks.stopStateUpdates()
        }
    }
    lifecycle.addObserver(observer)
}

fun <T> T.useBackgroundEffect(
    vararg values: Any?,
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    cancelWhenRerun: Boolean = false,
    block: suspend CoroutineScope.() -> Unit
) where T : LifecycleOwner, T : ReactiveComponent {
    useEffectWithCleanup(*values) {
        val job = inBackground(state, cancelWhenBelowState, block)
        return@useEffectWithCleanup {
            if (cancelWhenRerun) {
                job.cancel()
            }
        }
    }
}

fun <C> C.useBackgroundCallback(
    vararg values: Any?,
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    callback: suspend CoroutineScope.() -> Unit
): () -> Job where C : LifecycleOwner, C : ReactiveComponent {
    return useMemo(*values) { { inBackground(state, cancelWhenBelowState, callback) } }
}

fun <C, R> C.useBackgroundCallback(
    vararg values: Any?,
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    callback: suspend CoroutineScope.(R) -> Unit
): (R) -> Job where C : LifecycleOwner, C : ReactiveComponent {
    return useMemo(*values) {
        { p1 ->
            inBackground(state, cancelWhenBelowState) {
                callback(p1)
            }
        }
    }
}

fun <C, R, S> C.useBackgroundCallback(
    vararg values: Any?,
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    callback: suspend CoroutineScope.(R, S) -> Unit
): (R, S) -> Job where C : LifecycleOwner, C : ReactiveComponent {
    return useMemo(*values) {
        { p1, p2 ->
            inBackground(state, cancelWhenBelowState) {
                callback(p1, p2)
            }
        }
    }
}

fun <C, R, S, U> C.useBackgroundCallback(
    vararg values: Any?,
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    callback: suspend CoroutineScope.(R, S, U) -> Unit
): (R, S, U) -> Job where C : LifecycleOwner, C : ReactiveComponent {
    return useMemo(*values) {
        { p1, p2, p3 ->
            inBackground(state, cancelWhenBelowState) {
                callback(p1, p2, p3)
            }
        }
    }
}

fun <C, R, S, U, V> C.useBackgroundCallback(
    vararg values: Any?,
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    callback: suspend CoroutineScope.(R, S, U, V) -> Unit
): (R, S, U, V) -> Job where C : LifecycleOwner, C : ReactiveComponent {
    return useMemo(*values) {
        { p1, p2, p3, p4 ->
            inBackground(
                state,
                cancelWhenBelowState,
            ) {
                callback(p1, p2, p3, p4)
            }
        }
    }
}

fun <C, R, S, U, V, W> C.useBackgroundCallback(
    vararg values: Any?,
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    callback: suspend CoroutineScope.(R, S, U, V, W) -> Unit
): (R, S, U, V, W) -> Job where C : LifecycleOwner, C : ReactiveComponent {
    return useMemo(*values) {
        { p1, p2, p3, p4, p5 ->
            inBackground(
                state,
                cancelWhenBelowState,
            ) {
                callback(p1, p2, p3, p4, p5)
            }
        }
    }
}

fun <T : ITopic, V> AndromedaFragment.useTopic(topic: T, default: V, mapper: (T) -> V): V {
    val (state, setState) = useState(default)

    // Note: This does not change when the mapper changes
    useEffect(topic) {
        observe(topic) {
            setState(mapper(topic))
        }
    }

    return state
}

fun <T : ITopic, V> AndromedaFragment.useTopic(topic: T, mapper: (T) -> V?): V? {
    return useTopic(topic, null, mapper)
}

fun <T : Any, V> AndromedaFragment.useTopic(
    topic: com.kylecorry.andromeda.core.topics.generic.ITopic<T>,
    default: V,
    mapper: (T) -> V
): V {
    val (state, setState) = useState(default)

    // Note: This does not change when the mapper changes
    useEffect(topic) {
        observe(topic) {
            setState(mapper(it))
        }
    }

    return state
}

fun <T : Any, V> AndromedaFragment.useTopic(
    topic: com.kylecorry.andromeda.core.topics.generic.ITopic<T>,
    mapper: (T) -> V?
): V? {
    return useTopic(topic, null, mapper)
}
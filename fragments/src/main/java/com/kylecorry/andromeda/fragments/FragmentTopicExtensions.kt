package com.kylecorry.andromeda.fragments

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.kylecorry.andromeda.core.coroutines.BackgroundMinimumState
import com.kylecorry.andromeda.core.subscriptions.ISubscription
import com.kylecorry.andromeda.core.topics.ITopic
import com.kylecorry.andromeda.core.topics.asLiveData
import com.kylecorry.andromeda.core.topics.generic.asLiveData
import com.kylecorry.andromeda.core.ui.ReactiveComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

fun <T> Fragment.observe(liveData: LiveData<T>, listener: (T) -> Unit) {
    liveData.observe(viewLifecycleOwner) {
        listener(it)
    }
}

fun Fragment.observe(topic: ITopic, listener: () -> Unit) {
    topic.asLiveData().observe(viewLifecycleOwner) {
        listener()
    }
}

fun <T : Any> Fragment.observe(
    topic: com.kylecorry.andromeda.core.topics.generic.ITopic<T>,
    listener: (T) -> Unit
) {
    observe(topic.asLiveData(), listener)
}

fun Fragment.observe(
    subscription: ISubscription,
    state: BackgroundMinimumState = BackgroundMinimumState.Any,
    collectOn: CoroutineContext = Dispatchers.Default,
    observeOn: CoroutineContext = Dispatchers.Main,
    listener: suspend () -> Unit
) {
    observeFlow(subscription.flow(), state, collectOn, observeOn) { listener() }
}

fun <T> Fragment.observe(
    subscription: com.kylecorry.andromeda.core.subscriptions.generic.ISubscription<T>,
    state: BackgroundMinimumState = BackgroundMinimumState.Any,
    collectOn: CoroutineContext = Dispatchers.Default,
    observeOn: CoroutineContext = Dispatchers.Main,
    listener: suspend (T) -> Unit
) {
    observeFlow(subscription.flow(), state, collectOn, observeOn, listener)
}

fun <T> Fragment.observeFlow(
    flow: Flow<T>,
    state: BackgroundMinimumState = BackgroundMinimumState.Any,
    collectOn: CoroutineContext = Dispatchers.Default,
    observeOn: CoroutineContext = Dispatchers.Main,
    listener: suspend (T) -> Unit
) {
    repeatInBackground(state) {
        withContext(collectOn) {
            flow.collect {
                withContext(observeOn) {
                    listener(it)
                }
            }
        }
    }
}

fun <T : View> ReactiveComponent.useViewWithCleanup(
    id: Int,
    lifecycleHookTrigger: LifecycleHookTrigger,
    cleanup: (T) -> Unit
): T {
    val view = useView<T>(id)
    useEffectWithCleanup(lifecycleHookTrigger.onResume(), view) {
        return@useEffectWithCleanup {
            cleanup(view)
        }
    }
    return view
}
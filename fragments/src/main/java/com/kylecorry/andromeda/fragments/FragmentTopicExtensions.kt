package com.kylecorry.andromeda.fragments

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.kylecorry.andromeda.core.coroutines.BackgroundMinimumState
import com.kylecorry.andromeda.core.ui.ReactiveComponent
import com.kylecorry.luna.concurrency.IFlowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

fun <T> Fragment.observe(liveData: LiveData<T>, listener: (T) -> Unit) {
    liveData.observe(viewLifecycleOwner) {
        listener(it)
    }
}

fun <T> Fragment.observe(
    topic: IFlowable<T>,
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    collectOn: CoroutineContext = Dispatchers.Default,
    observeOn: CoroutineContext = Dispatchers.Main,
    listener: suspend () -> Unit
) {
    observeFlow(topic.flow, state, collectOn, observeOn) {
        listener()
    }
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

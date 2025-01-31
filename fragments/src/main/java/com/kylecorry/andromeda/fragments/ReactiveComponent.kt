package com.kylecorry.andromeda.fragments

import android.content.Context
import android.view.View
import android.widget.TextView
import com.kylecorry.luna.annotations.ExperimentalUsage
import com.kylecorry.luna.hooks.State

interface ReactiveComponent {
    fun useContext(): Context
    fun useEffect(vararg values: Any?, action: () -> Unit)
    fun <T> useMemo(vararg values: Any?, value: () -> T): T
    fun <T> useState(initialValue: T): State<T>
}

@ExperimentalUsage("This is not ready for use, the signature will change")
fun ReactiveComponent.Text(text: String): View {
    val context = useContext()
    val view = useMemo(context) { TextView(context) }
    useEffect(view, text) {
        view.text = text
    }
    return view
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
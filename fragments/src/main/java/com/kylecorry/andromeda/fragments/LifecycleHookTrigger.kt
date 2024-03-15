package com.kylecorry.andromeda.fragments

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class LifecycleHookTrigger {

    private var onCreateCount = 0
    private var onStartCount = 0
    private var onResumeCount = 0

    private val observer = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> onCreateCount++
            Lifecycle.Event.ON_START -> onStartCount++
            Lifecycle.Event.ON_RESUME -> onResumeCount++
            else -> {
                // Do nothing
            }
        }
    }

    internal fun bind(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(observer)
    }

    internal fun unbind(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.removeObserver(observer)
    }

    fun onCreate(): Int {
        return onCreateCount
    }

    fun onStart(): Int {
        return onStartCount
    }

    fun onResume(): Int {
        return onResumeCount
    }

}
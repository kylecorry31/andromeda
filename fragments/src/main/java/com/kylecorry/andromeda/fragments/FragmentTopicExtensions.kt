package com.kylecorry.andromeda.fragments

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.kylecorry.andromeda.core.topics.ITopic
import com.kylecorry.andromeda.core.topics.asLiveData
import com.kylecorry.andromeda.core.topics.generic.asLiveData

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

fun <T> Fragment.observe(
    topic: com.kylecorry.andromeda.core.topics.generic.ITopic<T>,
    listener: (T) -> Unit
) {
    observe(topic.asLiveData(), listener)
}
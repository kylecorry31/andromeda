package com.kylecorry.andromeda.fragments

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kylecorry.luna.topics.ITopic

fun <T : ITopic> T.asLiveData(): LiveData<T> {
    lateinit var liveData: MutableLiveData<T>
    val handler = Handler(Looper.getMainLooper())
    val lock = Any()

    val callback: () -> Boolean = {
        handler.post {
            synchronized(lock) {
                liveData.value = this@asLiveData
            }
        }
        true
    }

    liveData = object : MutableLiveData<T>(null) {
        override fun onActive() {
            super.onActive()
            subscribe(callback)
        }

        override fun onInactive() {
            super.onInactive()
            unsubscribe(callback)
        }
    }

    return liveData
}

fun <K : Any, T : com.kylecorry.luna.topics.generic.ITopic<K>> T.asLiveData(): LiveData<K> {
    lateinit var liveData: MutableLiveData<K>
    val handler = Handler(Looper.getMainLooper())
    val lock = Any()

    val callback: (K) -> Boolean = {
        handler.post {
            synchronized(lock) {
                liveData.value = it
            }
        }
        true
    }

    liveData = object : MutableLiveData<K>(null) {
        override fun onActive() {
            super.onActive()
            subscribe(callback)
        }

        override fun onInactive() {
            super.onInactive()
            unsubscribe(callback)
        }
    }

    return liveData
}

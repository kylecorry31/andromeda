package com.kylecorry.andromeda.core.topics.generic

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.*

typealias Subscriber<T> = (T) -> Boolean

interface ITopic<T> {
    val value: Optional<T>

    fun subscribe(subscriber: Subscriber<T>)
    fun unsubscribe(subscriber: Subscriber<T>)
    fun unsubscribeAll()
    suspend fun read(): T
}

fun <K, T : ITopic<K>> T.asLiveData(): LiveData<K> {
    lateinit var liveData: MutableLiveData<K>
    val handler = Handler(Looper.getMainLooper())
    val lock = Object()

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
package com.kylecorry.andromeda.core.topics

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

typealias Subscriber = () -> Boolean

interface ITopic {
    fun subscribe(subscriber: Subscriber)
    fun unsubscribe(subscriber: Subscriber)
    fun unsubscribeAll()
    suspend fun read()
}

fun <T : ITopic> T.asLiveData(): LiveData<T> {
    lateinit var liveData: MutableLiveData<T>
    val handler = Handler(Looper.getMainLooper())
    val lock = Object()

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
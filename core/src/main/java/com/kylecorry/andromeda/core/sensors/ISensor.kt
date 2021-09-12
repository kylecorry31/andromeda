package com.kylecorry.andromeda.core.sensors

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface ISensor {

    val quality: Quality

    val hasValidReading: Boolean

    fun start(listener: SensorListener)

    fun stop(listener: SensorListener?)

    suspend fun read()
}

fun <T : ISensor> T.asLiveData(): LiveData<T> {
    lateinit var liveData: MutableLiveData<T>
    val handler = Handler(Looper.getMainLooper())
    val lock = Object()

    val callback: () -> Boolean = {
        handler.post {
            synchronized(lock) {
                liveData.value = this
            }
        }
        true
    }

    liveData = object : MutableLiveData<T>(null) {
        override fun onActive() {
            super.onActive()
            start(callback)
        }

        override fun onInactive() {
            super.onInactive()
            stop(callback)
        }
    }

    return liveData
}
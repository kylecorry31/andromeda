package com.kylecorry.andromeda.core.sensors

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

abstract class AbstractSensor : ISensor {

    override val quality = Quality.Unknown

    private val listeners = mutableSetOf<SensorListener>()
    private var started = false

    override fun start(listener: SensorListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
        if (started) return
        startImpl()
        started = true
    }

    override fun stop(listener: SensorListener?) {
        synchronized(listeners) {
            if (listener != null) {
                listeners.remove(listener)
            } else {
                listeners.clear()
            }
        }
        if (listeners.isNotEmpty()) return
        if (!started) return
        stopImpl()
        started = false
    }
    
    override suspend fun read() = suspendCancellableCoroutine<Unit> { cont ->
        val callback: () -> Boolean = {
            cont.resume(Unit)
            false
        }
        cont.invokeOnCancellation {
            stop(callback)
        }
        start(callback)
    }

    protected abstract fun startImpl()
    protected abstract fun stopImpl()

    protected fun notifyListeners() {
        synchronized(listeners) {
            val finishedListeners = listeners.filter { !it.invoke() }
            finishedListeners.forEach(::stop)
        }
    }

}
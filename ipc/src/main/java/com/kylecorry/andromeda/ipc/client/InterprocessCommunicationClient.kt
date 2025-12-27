package com.kylecorry.andromeda.ipc.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import androidx.core.os.bundleOf
import com.kylecorry.andromeda.ipc.CODE_INTERNAL_ERROR
import com.kylecorry.andromeda.ipc.CODE_SERVICE_UNAVAILABLE
import com.kylecorry.andromeda.ipc.InterprocessCommunicationRequest
import com.kylecorry.andromeda.ipc.InterprocessCommunicationResponse
import com.kylecorry.andromeda.ipc.PROPERTY_CODE
import com.kylecorry.andromeda.ipc.PROPERTY_HEADERS
import com.kylecorry.andromeda.ipc.PROPERTY_PAYLOAD
import com.kylecorry.andromeda.ipc.PROPERTY_ROUTE
import com.kylecorry.luna.coroutines.onDefault
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.io.Closeable
import java.time.Duration
import kotlin.coroutines.resume

class InterprocessCommunicationClient(
    private val context: Context,
    private val intent: Intent,
    private val looper: Looper = Looper.getMainLooper()
) : Closeable {
    private var isBound = false
    private var isConnecting = false
    private var lock = Any()

    private var messenger: Messenger? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            synchronized(lock) {
                messenger = Messenger(binder)
                isBound = true
                isConnecting = false
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            synchronized(lock) {
                messenger = null
                isBound = false
                isConnecting = false
            }
        }
    }

    fun connect(): Boolean {
        synchronized(lock) {
            if (isConnecting || isBound) {
                return true
            }
            isConnecting = true
        }
        return context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun isConnected(): Boolean {
        synchronized(lock) {
            return isBound
        }
    }

    suspend fun waitUntilConnected(timeoutMillis: Long = 1000): Unit = onDefault {
        var isNowConnected = false
        synchronized(lock) {
            if (!isConnecting && !isBound) {
                throw IllegalStateException("Service is not connected")
            }
            isNowConnected = isBound
        }

        withTimeout(timeoutMillis) {
            while (!isNowConnected) {
                synchronized(lock) {
                    isNowConnected = isBound
                }
                delay(20)
            }
        }
    }

    suspend fun send(
        path: String,
        request: InterprocessCommunicationRequest = InterprocessCommunicationRequest(),
    ): InterprocessCommunicationResponse =
        suspendCancellableCoroutine {
            val message = Message.obtain()
            val bundle = message.data
            bundle.putString(PROPERTY_ROUTE, path)
            bundle.putBundle(PROPERTY_HEADERS, request.headers)
            bundle.putByteArray(PROPERTY_PAYLOAD, request.payload)

            val replyHandler = object : Handler(looper) {
                override fun handleMessage(msg: Message) {
                    it.resume(
                        InterprocessCommunicationResponse(
                            msg.data.getInt(PROPERTY_CODE, CODE_INTERNAL_ERROR),
                            msg.data.getBundle(PROPERTY_HEADERS) ?: bundleOf(),
                            msg.data.getByteArray(PROPERTY_PAYLOAD)
                        )
                    )
                }
            }

            val replyMessenger = Messenger(replyHandler)
            message.replyTo = replyMessenger
            messenger?.send(message) ?: it.resume(
                InterprocessCommunicationResponse(
                    CODE_SERVICE_UNAVAILABLE,
                    bundleOf(),
                    null
                )
            )
        }

    suspend fun connectAndSend(
        route: String,
        request: InterprocessCommunicationRequest = InterprocessCommunicationRequest(),
        timeout: Duration = Duration.ofSeconds(10),
        stayConnected: Boolean = false
    ): InterprocessCommunicationResponse {
        return try {
            val success = connect()
            if (!success) {
                throw IllegalStateException("Could not connect to service")
            }
            waitUntilConnected(timeout.toMillis())
            send(route, request)
        } finally {
            if (!stayConnected) {
                close()
            }
        }
    }

    override fun close() {
        synchronized(lock) {
            if (!isBound && !isConnecting) {
                return
            }
            context.unbindService(connection)
            messenger = null
            isBound = false
            isConnecting = false
        }
    }
}
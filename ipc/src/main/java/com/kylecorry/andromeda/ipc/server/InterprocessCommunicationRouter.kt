package com.kylecorry.andromeda.ipc.server

import android.content.Context
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import androidx.core.os.bundleOf
import com.kylecorry.andromeda.ipc.InterprocessCommunicationRequest
import com.kylecorry.andromeda.ipc.InterprocessCommunicationResponse
import com.kylecorry.andromeda.ipc.PROPERTY_CODE
import com.kylecorry.andromeda.ipc.PROPERTY_HEADERS
import com.kylecorry.andromeda.ipc.PROPERTY_PAYLOAD
import com.kylecorry.andromeda.ipc.PROPERTY_ROUTE
import com.kylecorry.luna.coroutines.CoroutineQueueRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Closeable

class InterprocessCommunicationRouter(
    private val routes: Map<String, suspend (context: Context, request: InterprocessCommunicationRequest) -> InterprocessCommunicationResponse>,
    private val looper: Looper = Looper.getMainLooper(),
    queueSize: Int = 256
) : Closeable {

    private var messenger: Messenger? = null

    private val scope = CoroutineScope(Dispatchers.Default)
    private val runner = CoroutineQueueRunner(queueSize)

    private fun createHandler(context: Context): Handler {
        return object : Handler(looper) {
            private val applicationContext = context.applicationContext

            override fun handleMessage(msg: Message) {
                val data = msg.data
                val route = data.getString(PROPERTY_ROUTE) ?: run {
                    super.handleMessage(msg)
                    return
                }

                val action = routes[route] ?: run {
                    super.handleMessage(msg)
                    return
                }

                val replyTo = msg.replyTo

                scope.launch {
                    runner.enqueue {
                        val response =
                            action(
                                applicationContext, InterprocessCommunicationRequest(
                                    data.getBundle(PROPERTY_HEADERS) ?: bundleOf(),
                                    data.getByteArray(PROPERTY_PAYLOAD)
                                )
                            )
                        if (replyTo != null) {
                            val reply = Message.obtain()
                            val bundle = reply.data
                            bundle.putInt(PROPERTY_CODE, response.code)
                            bundle.putBundle(PROPERTY_HEADERS, response.headers)
                            bundle.putByteArray(PROPERTY_PAYLOAD, response.payload)
                            replyTo.send(reply)
                        }
                    }
                }
            }
        }
    }

    fun bind(context: Context): IBinder? {
        messenger = Messenger(createHandler(context))
        return messenger?.binder
    }

    fun unbind() {
        messenger = null
        runner.cancel()
    }

    override fun close() {
        unbind()
    }
}
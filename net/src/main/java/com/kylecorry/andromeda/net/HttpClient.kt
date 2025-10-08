package com.kylecorry.andromeda.net

import com.kylecorry.luna.coroutines.onIO
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration

class HttpClient {

    suspend fun send(
        url: String,
        method: HttpMethod = HttpMethod.GET,
        body: ByteArray? = null,
        headers: Map<String, String> = emptyMap(),
        readTimeout: Duration? = null,
        connectTimeout: Duration? = null,
        followRedirects: Boolean = true
    ): HttpResponse = onIO {
        val url = URL(url)
        val connection = url.openConnection() as HttpURLConnection
        if (readTimeout != null) {
            connection.readTimeout = readTimeout.toMillis().toInt()
        }
        if (connectTimeout != null) {
            connection.connectTimeout = connectTimeout.toMillis().toInt()
        }
        for ((key, value) in headers) {
            connection.setRequestProperty(key, value)
        }
        connection.requestMethod = method.name
        connection.instanceFollowRedirects = followRedirects
        if (body != null) {
            connection.doOutput = true
            connection.outputStream.use {
                it.write(body)
            }
        }
        connection.connect()
        val bytes = connection.getInputStream().use { it.readBytes() }
        val responseHeaders = connection.headerFields.filterKeys { it != null }
        val responseCode = connection.responseCode
        connection.disconnect()
        HttpResponse(responseCode, responseHeaders, bytes)
    }
}
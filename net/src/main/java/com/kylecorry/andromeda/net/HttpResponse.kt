package com.kylecorry.andromeda.net

class HttpResponse(
    val code: Int,
    val headers: Map<String, List<String>>,
    val content: ByteArray?
) {
    fun contentAsString(): String? {
        return content?.toString(Charsets.UTF_8)
    }

    fun isSuccessful(): Boolean {
        return code in 200..299
    }
}
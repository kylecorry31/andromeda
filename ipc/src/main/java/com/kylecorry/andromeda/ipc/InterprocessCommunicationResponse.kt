package com.kylecorry.andromeda.ipc

import android.os.Bundle

class InterprocessCommunicationResponse(
    val code: Int,
    val headers: Bundle,
    val payload: ByteArray?
)
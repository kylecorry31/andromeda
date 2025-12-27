package com.kylecorry.andromeda.ipc

import android.os.Bundle
import androidx.core.os.bundleOf

class InterprocessCommunicationRequest(
    val headers: Bundle = bundleOf(),
    val payload: ByteArray? = null
)
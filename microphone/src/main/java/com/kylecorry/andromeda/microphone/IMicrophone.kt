package com.kylecorry.andromeda.microphone

import com.kylecorry.andromeda.core.sensors.ISensor

interface IMicrophone: ISensor {
    val audio: ShortArray?
    val amplitude: Short?
}
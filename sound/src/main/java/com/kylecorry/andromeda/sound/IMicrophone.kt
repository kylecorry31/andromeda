package com.kylecorry.andromeda.sound

import com.kylecorry.andromeda.core.sensors.ISensor

interface IMicrophone: ISensor {
    val audio: ShortArray?
    val amplitude: Short?
}
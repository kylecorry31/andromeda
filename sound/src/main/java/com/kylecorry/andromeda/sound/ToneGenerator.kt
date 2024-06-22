package com.kylecorry.andromeda.sound

import android.media.AudioTrack
import kotlin.math.sin

class ToneGenerator {

    private val soundGenerator = SoundGenerator()

    fun getTone(frequency: Int, sampleRate: Int = 64000, durationSeconds: Float = 1f): AudioTrack {
        return soundGenerator.getSound(sampleRate, durationSeconds) {
            sin(frequency * 2 * Math.PI * it / sampleRate)
        }
    }

}
package com.kylecorry.andromeda.sound

import android.media.*

class Speaker(private val sampleRate: Int) {

    private var player: AudioTrack? = null

    fun start() {
        val channel_out = AudioFormat.CHANNEL_OUT_MONO
        val format = AudioFormat.ENCODING_PCM_16BIT
        val minBuffer =
            AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, format)

        player = AudioTrack(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
            AudioFormat.Builder().setEncoding(format).setSampleRate(sampleRate)
                .setChannelMask(channel_out).build(),
            minBuffer,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )

        player?.play()
    }

    fun play(audioData: ShortArray){
        player?.write(audioData, 0, audioData.size)
    }

    fun stop(){
        player?.stop()
        player?.release()
        player = null
    }

}
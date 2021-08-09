package com.kylecorry.andromeda.sound

import android.media.AudioTrack
import com.kylecorry.andromeda.core.time.Timer

open class SoundPlayer(private val sound: AudioTrack): ISoundPlayer {

    private var volume = 0f

    private var releaseWhenOff = false

    private val fadeOffIntervalometer = Timer {
        volume -= 0.1f
        sound.setVolume(volume.coerceIn(0f, 1f))
        if (volume <= 0f){
            if (releaseWhenOff) release() else off()
        }
    }

    private val fadeOnTimer: Timer = Timer {
        volume += 0.1f
        sound.setVolume(volume.coerceIn(0f, 1f))
        if (volume >= 1f){
            stopFadeOn()
        }
    }

    override fun on() {
        if (isOn()){
            return
        }
        volume = 1f
        setVolume(volume)
        sound.play()
        fadeOffIntervalometer.stop()
        fadeOnTimer.stop()
    }

    override fun fadeOn(){
        if (isOn()){
            return
        }
        volume = 0f
        setVolume(0f)
        sound.play()
        fadeOffIntervalometer.stop()
        fadeOnTimer.interval(20)
    }

    override fun off() {
        if (!isOn()){
            return
        }
        fadeOffIntervalometer.stop()
        fadeOnTimer.stop()
        sound.pause()
    }

    override fun fadeOff(releaseWhenOff: Boolean){
        if (!isOn()){
            return
        }
        this.releaseWhenOff = releaseWhenOff
        fadeOnTimer.stop()
        fadeOffIntervalometer.interval(20)
    }

    override fun isOn(): Boolean {
        return sound.playState == AudioTrack.PLAYSTATE_PLAYING
    }

    override fun release() {
        off()
        sound.release()
    }

    override fun setVolume(volume: Float){
        fadeOffIntervalometer.stop()
        fadeOnTimer.stop()
        sound.setVolume(volume.coerceIn(0f, 1f))
    }

    private fun stopFadeOn(){
        fadeOnTimer.stop()
    }
}
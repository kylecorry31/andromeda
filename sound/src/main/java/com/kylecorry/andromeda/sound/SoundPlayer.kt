package com.kylecorry.andromeda.sound

import android.media.AudioTrack
import com.kylecorry.andromeda.core.time.Intervalometer

open class SoundPlayer(private val sound: AudioTrack): ISoundPlayer {

    private var volume = 0f

    private var releaseWhenOff = false

    private val fadeOffIntervalometer = Intervalometer {
        volume -= 0.1f
        sound.setVolume(volume.coerceIn(0f, 1f))
        if (volume <= 0f){
            if (releaseWhenOff) release() else off()
        }
    }

    private val fadeOnIntervalometer: Intervalometer = Intervalometer {
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
        fadeOnIntervalometer.stop()
    }

    override fun fadeOn(){
        if (isOn()){
            return
        }
        volume = 0f
        setVolume(0f)
        sound.play()
        fadeOffIntervalometer.stop()
        fadeOnIntervalometer.interval(20)
    }

    override fun off() {
        if (!isOn()){
            return
        }
        fadeOffIntervalometer.stop()
        fadeOnIntervalometer.stop()
        sound.pause()
    }

    override fun fadeOff(releaseWhenOff: Boolean){
        if (!isOn()){
            return
        }
        this.releaseWhenOff = releaseWhenOff
        fadeOnIntervalometer.stop()
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
        fadeOnIntervalometer.stop()
        sound.setVolume(volume.coerceIn(0f, 1f))
    }

    private fun stopFadeOn(){
        fadeOnIntervalometer.stop()
    }
}
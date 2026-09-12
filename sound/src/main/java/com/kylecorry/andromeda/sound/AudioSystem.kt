package com.kylecorry.andromeda.sound

import android.content.Context
import android.media.AudioDeviceInfo

import android.media.AudioManager
import androidx.core.content.getSystemService

object AudioSystem {

    fun hasWiredHeadphones(context: Context): Boolean {
        val devices = getAudioDevices(context)
        return devices.any { it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES || it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET }
    }

    fun hasWiredHeadset(context: Context): Boolean {
        val devices = getAudioDevices(context)
        return devices.any { it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET }
    }

    fun getSpeakers(context: Context): List<AudioDeviceInfo> {
        val audioManager = context.getSystemService<AudioManager>() ?: return emptyList()
        return audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList()
    }

    fun getMicrophones(context: Context): List<AudioDeviceInfo> {
        val audioManager = context.getSystemService<AudioManager>() ?: return emptyList()
        return audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS).toList()
    }

    fun getAudioDevices(context: Context): List<AudioDeviceInfo> {
        val audioManager = context.getSystemService<AudioManager>() ?: return emptyList()
        return audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS or AudioManager.GET_DEVICES_INPUTS)
            .toList()
    }
}

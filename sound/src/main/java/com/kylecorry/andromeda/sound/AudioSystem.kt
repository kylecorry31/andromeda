package com.kylecorry.andromeda.sound

import android.content.Context
import android.media.AudioDeviceInfo

import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi

import androidx.core.content.getSystemService

object AudioSystem {

    @RequiresApi(Build.VERSION_CODES.M)
    fun hasWiredHeadphones(context: Context): Boolean {
        val devices = getAudioDevices(context)
        return devices.any { it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES || it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun hasWiredHeadset(context: Context): Boolean {
        val devices = getAudioDevices(context)
        return devices.any { it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getSpeakers(context: Context): List<AudioDeviceInfo> {
        val audioManager = context.getSystemService<AudioManager>() ?: return emptyList()
        return audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getMicrophones(context: Context): List<AudioDeviceInfo> {
        val audioManager = context.getSystemService<AudioManager>() ?: return emptyList()
        return audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS).toList()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getAudioDevices(context: Context): List<AudioDeviceInfo> {
        val audioManager = context.getSystemService<AudioManager>() ?: return emptyList()
        return audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS or AudioManager.GET_DEVICES_INPUTS)
            .toList()
    }
}
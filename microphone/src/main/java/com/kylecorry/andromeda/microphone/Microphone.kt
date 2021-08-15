package com.kylecorry.andromeda.microphone

import android.annotation.SuppressLint
import android.content.Context
import android.media.*
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.permissions.Permissions
import kotlin.math.abs


class Microphone(private val context: Context, private val sampleRate: Int) : AbstractSensor(),
    IMicrophone {
    private var recorder: AudioRecord? = null
    private var buffer: ShortArray? = null
    private var _hasReading = false

    private val bufferLock = Object()

    override val audio: ShortArray?
        get() {
            val b = synchronized(bufferLock) {
                buffer?.clone()
            }
            return if (b != null) {
                _hasReading = true
                recorder!!.read(b, 0, b.size)
                b
            } else {
                null
            }
        }

    override val amplitude: Short?
        get() {
            return synchronized(bufferLock) {
                val b = buffer ?: return@synchronized null
                var max = 0
                for (s in b) {
                    if (abs(s.toInt()) > max) {
                        max = abs(s.toInt())
                    }
                }
                return@synchronized max.toShort()
            }
        }

    @SuppressLint("MissingPermission")
    override fun startImpl() {
        if (!Permissions.canRecordAudio(context)) {
            return
        }

        val minSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        synchronized(bufferLock) {
            buffer = ShortArray(minSize)
        }
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minSize
        )
        recorder?.startRecording()

        recorder?.audioSessionId?.let {
            if (AcousticEchoCanceler.isAvailable()) {
                val echo = AcousticEchoCanceler.create(it)
                echo.enabled = false
            }
            if (NoiseSuppressor.isAvailable()) {
                val noise = NoiseSuppressor.create(it)
                noise.enabled = false
            }
            if (AutomaticGainControl.isAvailable()) {
                val gain = AutomaticGainControl.create(it)
                gain.enabled = false
            }
        }

    }

    override fun stopImpl() {
        recorder?.stop()
        recorder = null
        synchronized(bufferLock) {
            buffer = null
        }
    }

    override val hasValidReading: Boolean
        get() = _hasReading
}
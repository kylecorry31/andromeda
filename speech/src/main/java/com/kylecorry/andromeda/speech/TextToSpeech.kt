package com.kylecorry.andromeda.speech

import android.content.Context
import java.util.*
import android.speech.tts.TextToSpeech as TTS


class TextToSpeech(private val context: Context) : ITextToSpeech {

    private var tts: TTS? = null

    override fun speak(text: String, locale: Locale?) {
        cancel()
        tts = TTS(
            context
        ) { status ->
            if (status != TTS.ERROR) {
                tts?.language = locale ?: Locale.getDefault()
                tts?.speak(
                    text,
                    TTS.QUEUE_FLUSH,
                    null,
                    null
                )
            }
        }
    }

    override fun cancel() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

}
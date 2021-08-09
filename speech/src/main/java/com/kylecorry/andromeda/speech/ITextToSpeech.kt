package com.kylecorry.andromeda.speech

import java.util.*

interface ITextToSpeech {
    fun speak(text: String, locale: Locale? = null)
    fun cancel()
}
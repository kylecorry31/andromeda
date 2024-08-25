package com.kylecorry.andromeda.markdown

import android.text.Spannable

class MarkdownExtension(
    val length: Int,
    val openingCharacter: Char,
    val closingCharacter: Char = openingCharacter,
    val spanProducer: () -> Any
)
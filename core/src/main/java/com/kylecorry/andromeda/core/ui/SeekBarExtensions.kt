package com.kylecorry.andromeda.core.ui

import android.widget.SeekBar

fun SeekBar.setOnProgressChangeListener(listener: (progress: Int, fromUser: Boolean) -> Unit) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            listener(progress, fromUser)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }

    })
}
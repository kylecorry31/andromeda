package com.kylecorry.andromeda.forms

import android.view.View

abstract class FormField<T>(
    val id: String,
    val view: View,
) {
    abstract var value: T
}
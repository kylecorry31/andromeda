package com.kylecorry.andromeda.views.reactivity

import android.content.Context
import android.view.View

data class VDOMNode<T, V>(
    val domClass: Class<T>,
    val attributes: V,
    val children: List<VDOMNode<*, *>?> = emptyList(),
    val managesOwnChildren: Boolean = false,
    val create: (context: Context) -> T,
    val update: (view: T, attributes: V) -> Unit
) where T : View, V : ViewAttributes
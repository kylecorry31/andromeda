package com.kylecorry.andromeda.core

fun tryOrNothing(block: () -> Unit){
    try {
        block()
    } catch (e: Exception){}
}
package com.kylecorry.andromeda.pickers

data class MenuItem(val name: CharSequence, val action: () -> Boolean)

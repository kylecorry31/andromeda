package com.kylecorry.andromeda.views.list

interface ListItemMapper<T> {
    fun map(value: T): ListItem
}
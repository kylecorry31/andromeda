package com.kylecorry.andromeda.views.list

import com.google.android.flexbox.AlignContent
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.JustifyContent

/**
 * Alignment for list item data
 * @property justifyContent The horizontal alignment of the data
 * @property alignItems The vertical alignment of the data
 * @property alignContent The vertical alignment of the data when there are multiple rows
 */
data class ListItemDataAlignment(
    @JustifyContent val justifyContent: Int = JustifyContent.FLEX_START,
    @AlignItems val alignItems: Int = AlignItems.FLEX_START,
    @AlignContent val alignContent: Int = AlignContent.FLEX_START
)

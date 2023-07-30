package com.kylecorry.andromeda.gpx

import androidx.annotation.ColorInt

data class GPXTrack(
    val name: String? = null,
    val type: String? = null,
    val id: Long? = null,
    val comment: String? = null,
    @ColorInt val color: Int? = null,
    val lineStyle: String? = null,
    val group: String? = null,
    val segments: List<GPXTrackSegment> = emptyList()
)
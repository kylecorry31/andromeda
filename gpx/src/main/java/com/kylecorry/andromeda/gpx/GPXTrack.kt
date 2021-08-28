package com.kylecorry.andromeda.gpx

data class GPXTrack(
    val name: String?,
    val type: String?,
    val id: Long?,
    val comment: String?,
    val segments: List<GPXTrackSegment>
)
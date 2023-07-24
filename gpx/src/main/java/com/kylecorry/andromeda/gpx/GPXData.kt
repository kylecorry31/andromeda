package com.kylecorry.andromeda.gpx

data class GPXData(val waypoints: List<GPXWaypoint>, val tracks: List<GPXTrack>, val routes: List<GPXRoute>)
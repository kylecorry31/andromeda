package com.kylecorry.andromeda.core.system

import android.net.Uri
import android.os.Parcelable
import com.kylecorry.andromeda.core.units.Coordinate
import kotlinx.parcelize.Parcelize

object GeoUriParser {

    fun parse(data: Uri): NamedCoordinate? {
        val pattern = "geo:(-?[0-9]*\\.?[0-9]+),(-?[0-9]*\\.?[0-9]+)(?:\\?[\\w=&]*q=([^&]+))?"
        val qPattern = "(-?[0-9]*\\.?[0-9]+),(-?[0-9]*\\.?[0-9]+)(\\([^\\)]+\\))?"
        val regex = Regex(pattern)

        val matches = regex.find(data.toString())

        if (matches != null) {
            var lat = matches.groupValues[1].toDouble()
            var lng = matches.groupValues[2].toDouble()
            val q = matches.groupValues[3]
            var name: String? = null

            if (lat == 0.0 && lng == 0.0 && q.isNotEmpty()){
                val qRegex = Regex(qPattern)
                val qMatches = qRegex.find(q)
                if (qMatches != null){
                    lat = qMatches.groupValues[1].toDouble()
                    lng = qMatches.groupValues[2].toDouble()
                    name = Uri.decode(qMatches.groupValues[3]).replace('+', ' ')
                    if (name.length >= 2){
                        name = name.substring(1, name.length - 1)
                    }

                    if (name.isEmpty()){
                        name = null
                    }
                } else {
                    return null
                }
            }

            return NamedCoordinate(Coordinate(lat, lng), name)
        }

        return null
    }

    @Parcelize
    data class NamedCoordinate(val coordinate: Coordinate, val name: String? = null): Parcelable

}
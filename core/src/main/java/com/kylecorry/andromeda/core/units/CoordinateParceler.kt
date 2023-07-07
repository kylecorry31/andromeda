package com.kylecorry.andromeda.core.units

import android.os.Parcel
import com.kylecorry.sol.units.Coordinate
import kotlinx.parcelize.Parceler

object CoordinateParceler: Parceler<Coordinate> {
    override fun create(parcel: Parcel): Coordinate {
        return Coordinate(parcel.readDouble(), parcel.readDouble())
    }

    override fun Coordinate.write(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }
}
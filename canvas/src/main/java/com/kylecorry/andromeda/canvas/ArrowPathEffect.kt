package com.kylecorry.andromeda.canvas

import android.graphics.Path
import android.graphics.PathDashPathEffect

class ArrowPathEffect(size: Float = 6f, advance: Float = 3 * size, phase: Float = 0f) :
    PathDashPathEffect(
        getArrowPath(size), advance, phase, Style.ROTATE
    ) {

    companion object {
        private fun getArrowPath(size: Float): Path {
            val arrowPath = Path()
            arrowPath.moveTo(0f, -size)
            arrowPath.lineTo(size, 0f)
            arrowPath.lineTo(0f, size)
            arrowPath.close()
            return arrowPath
        }
    }
}
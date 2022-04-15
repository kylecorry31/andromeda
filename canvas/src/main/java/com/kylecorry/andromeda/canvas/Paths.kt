package com.kylecorry.andromeda.canvas

import android.graphics.Path
import kotlin.math.cos
import kotlin.math.sin

object Paths {

    fun dialTicks(
        x: Float,
        y: Float,
        radius: Float,
        tickLength: Float,
        spacing: Int,
        start: Int = 0,
        end: Int = 360,
        path: Path = Path()
    ): Path {
        return path.apply {
            reset()
            for (angle in start..end step spacing) {
                if (angle == end && start == end) {
                    continue
                }
                val tickX = cos(Math.toRadians(angle.toDouble()).toFloat())
                val tickY = sin(Math.toRadians(angle.toDouble()).toFloat())
                moveTo(x + tickX * (radius - tickLength), y + tickY * (radius - tickLength))
                lineTo(x + tickX * radius, y + tickY * radius)
            }
        }
    }

}
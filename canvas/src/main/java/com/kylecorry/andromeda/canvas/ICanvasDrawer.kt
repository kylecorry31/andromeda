package com.kylecorry.andromeda.canvas

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.PathEffect
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

interface ICanvasDrawer {
    var canvas: Canvas
    fun background(@ColorInt color: Int)
    fun clear()

    @ColorInt
    fun color(r: Int, g: Int = r, b: Int = g, a: Int? = null): Int
    fun fill(@ColorInt color: Int)
    fun tint(@ColorInt color: Int)
    fun noTint()
    fun stroke(@ColorInt color: Int)
    fun pathEffect(effect: PathEffect)
    fun noPathEffect()
    fun noStroke()
    fun noFill()
    fun strokeWeight(pixels: Float)
    fun strokeCap(cap: StrokeCap)
    fun strokeJoin(join: StrokeJoin)
    fun opacity(value: Int)
    fun erase()
    fun noErase()
    fun smooth()
    fun noSmooth()

    // TEXT HELPERS
    fun textAlign(align: TextAlign)
    fun textSize(pixels: Float)
    fun textStyle(style: TextStyle)
    fun textWidth(text: String): Float
    fun textHeight(text: String): Float
    fun textDimensions(text: String): Pair<Float, Float>
    fun pathWidth(path: Path): Float
    fun pathHeight(path: Path): Float
    fun pathDimensions(path: Path): Pair<Float, Float>
    fun textAscent(): Float
    fun textDescent(): Float
    fun text(str: String, x: Float, y: Float)

    // SHAPE HELPERS
    fun arc(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        start: Float,
        stop: Float,
        mode: ArcMode = ArcMode.Pie
    )

    fun ellipse(x: Float, y: Float, w: Float, h: Float = w)
    fun circle(x: Float, y: Float, diameter: Float)
    fun line(x1: Float, y1: Float, x2: Float, y2: Float)
    fun lines(points: FloatArray)
    fun grid(
        spacing: Float,
        width: Float = canvas.width.toFloat(),
        height: Float = canvas.height.toFloat()
    )

    fun point(x: Float, y: Float)

    // TODO: Support different radius for each corner
    fun rect(
        x: Float,
        y: Float,
        w: Float,
        h: Float = w,
        radius: Float = 0f
    )

    fun square(
        x: Float,
        y: Float,
        size: Float,
        radius: Float = 0f
    )

    fun path(value: Path)
    fun triangle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float)

    // Transforms
    // TODO: Add the transforms
    fun push()
    fun pop()
    fun rotate(
        degrees: Float,
        originX: Float = canvas.width / 2f,
        originY: Float = canvas.height / 2f
    )

    fun scale(x: Float, y: Float = x)
    fun scale(x: Float, y: Float = x, pivotX: Float, pivotY: Float)
    fun translate(x: Float, y: Float)
    fun loadImage(@DrawableRes id: Int, w: Int? = null, h: Int? = null): Bitmap
    fun image(
        img: Bitmap,
        x: Float,
        y: Float,
        w: Float = img.width.toFloat(),
        h: Float = img.height.toFloat()
    )

    fun image(
        img: Bitmap,
        dx: Float,
        dy: Float,
        dw: Float,
        dh: Float,
        sx: Float,
        sy: Float,
        sw: Float = img.width.toFloat(),
        sh: Float = img.height.toFloat()
    )

    fun imageMode(imageMode: ImageMode)
    fun textMode(textMode: TextMode)
    fun clip(path: Path)
    fun clipInverse(path: Path)
    fun mask(
        mask: Bitmap,
        tempBitmap: Bitmap = Bitmap.createBitmap(
            mask.width,
            mask.height,
            Bitmap.Config.ARGB_8888
        ),
        block: () -> Unit
    ): Bitmap

    fun dp(size: Float): Float
    fun sp(size: Float): Float
}
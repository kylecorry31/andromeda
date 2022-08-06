package com.kylecorry.andromeda.canvas

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap

class CanvasDrawer(private val context: Context, override var canvas: Canvas) : ICanvasDrawer {
    private val fillPaint = Paint().also {
        it.style = Paint.Style.FILL
    }
    private val strokePaint = Paint().also {
        it.style = Paint.Style.STROKE
    }
    private var paintStyle = PaintStyle.Fill
    private var imageMode = ImageMode.Corner
    private var textMode = TextMode.Corner
    private val measurementRect = Rect()
    private val measurementRectF = RectF()

    init {
        smooth()
        strokeCap(StrokeCap.Round)
        strokeJoin(StrokeJoin.Miter)
    }

    override fun background(@ColorInt color: Int) {
        canvas.drawColor(color)
    }

    override fun clear() {
        background(Color.TRANSPARENT)
    }

    @ColorInt
    override fun color(r: Int, g: Int, b: Int, a: Int?): Int {
        return if (a != null) {
            Color.argb(a, r, g, b)
        } else {
            Color.rgb(r, g, b)
        }
    }

    override fun fill(@ColorInt color: Int) {
        paintStyle = if (shouldStroke()) {
            PaintStyle.FillAndStroke
        } else {
            PaintStyle.Fill
        }
        fillPaint.color = color
    }

    override fun tint(@ColorInt color: Int) {
        fillPaint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        strokePaint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    override fun noTint() {
        fillPaint.colorFilter = null
        strokePaint.colorFilter = null
    }

    override fun stroke(@ColorInt color: Int) {
        paintStyle = if (shouldFill()) {
            PaintStyle.FillAndStroke
        } else {
            PaintStyle.Stroke
        }
        strokePaint.color = color
    }

    override fun pathEffect(effect: PathEffect) {
        strokePaint.pathEffect = effect
        fillPaint.pathEffect = effect
    }

    override fun noPathEffect() {
        strokePaint.pathEffect = null
        fillPaint.pathEffect = null
    }

    override fun noStroke() {
        paintStyle = if (shouldFill()) {
            PaintStyle.Fill
        } else {
            PaintStyle.None
        }
    }

    override fun noFill() {
        paintStyle = if (shouldStroke()) {
            PaintStyle.Stroke
        } else {
            PaintStyle.None
        }
    }

    override fun strokeWeight(pixels: Float) {
        strokePaint.strokeWidth = pixels
    }

    override fun strokeCap(cap: StrokeCap) {
        strokePaint.strokeCap = when (cap) {
            StrokeCap.Round -> Paint.Cap.ROUND
            StrokeCap.Square -> Paint.Cap.SQUARE
            StrokeCap.Project -> Paint.Cap.BUTT
        }
    }

    override fun strokeJoin(join: StrokeJoin) {
        strokePaint.strokeJoin = when (join) {
            StrokeJoin.Miter -> Paint.Join.MITER
            StrokeJoin.Bevel -> Paint.Join.BEVEL
            StrokeJoin.Round -> Paint.Join.ROUND
        }
    }

    override fun opacity(value: Int) {
        fillPaint.alpha = value
        strokePaint.alpha = value
    }

    override fun erase() {
        // This may need the following to be called in setup: setLayerType(LAYER_TYPE_HARDWARE, null)
        fillPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        strokePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun noErase() {
        fillPaint.xfermode = null
        strokePaint.xfermode = null
    }

    override fun smooth() {
        fillPaint.isAntiAlias = true
        strokePaint.isAntiAlias = true
    }

    override fun noSmooth() {
        fillPaint.isAntiAlias = false
        strokePaint.isAntiAlias = false
    }

    // TEXT HELPERS
    override fun textAlign(align: TextAlign) {
        val alignment = when (align) {
            TextAlign.Right -> Paint.Align.RIGHT
            TextAlign.Center -> Paint.Align.CENTER
            TextAlign.Left -> Paint.Align.LEFT
        }
        fillPaint.textAlign = alignment
        strokePaint.textAlign = alignment
    }

    override fun textSize(pixels: Float) {
        fillPaint.textSize = pixels
        strokePaint.textSize = pixels
    }

    override fun textStyle(style: TextStyle) {
        val typeface = when (style) {
            TextStyle.Normal -> fillPaint.setTypeface(
                Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.NORMAL
                )
            )
            TextStyle.Italic -> fillPaint.setTypeface(
                Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.ITALIC
                )
            )
            TextStyle.Bold -> fillPaint.setTypeface(
                Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.BOLD
                )
            )
            TextStyle.BoldItalic -> fillPaint.setTypeface(
                Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.BOLD_ITALIC
                )
            )
        }

        fillPaint.typeface = typeface
        strokePaint.typeface = typeface
    }

    override fun textWidth(text: String): Float {
        return textDimensions(text).first
    }

    override fun textHeight(text: String): Float {
        return textDimensions(text).second
    }

    override fun textDimensions(text: String): Pair<Float, Float> {
        // TODO: Factor in stroke
        fillPaint.getTextBounds(text, 0, text.length, measurementRect)
        return measurementRect.width().toFloat() to measurementRect.height().toFloat()
    }

    override fun pathWidth(path: Path): Float {
        return pathDimensions(path).first
    }

    override fun pathHeight(path: Path): Float {
        return pathDimensions(path).second
    }

    override fun pathDimensions(path: Path): Pair<Float, Float> {
        path.computeBounds(measurementRectF, true)
        return measurementRectF.width() to measurementRectF.height()
    }

    override fun textAscent(): Float {
        // TODO: Factor in stroke
        return fillPaint.ascent()
    }

    override fun textDescent(): Float {
        // TODO: Factor in stroke
        return fillPaint.descent()
    }

    override fun text(str: String, x: Float, y: Float) {
        if (!shouldDraw()) {
            return
        }

        val realX = if (textMode == TextMode.Center) {
            x - textWidth(str) / 2f
        } else {
            x
        }

        val realY = if (textMode == TextMode.Center) {
            y + textHeight(str) / 2f
        } else {
            y
        }

        if (shouldStroke()) {
            canvas.drawText(str, realX, realY, strokePaint)
        }

        if (shouldFill()) {
            canvas.drawText(str, realX, realY, fillPaint)
        }
    }


    // SHAPE HELPERS
    override fun arc(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        start: Float,
        stop: Float,
        mode: ArcMode
    ) {
        if (!shouldDraw()) {
            return
        }
        if (shouldFill()) {
            canvas.drawArc(x, y, x + w, y + h, start, stop - start, mode == ArcMode.Pie, fillPaint)
        }

        if (shouldStroke()) {
            canvas.drawArc(
                x,
                y,
                x + w,
                y + h,
                start,
                stop - start,
                mode == ArcMode.Pie,
                strokePaint
            )
        }
    }

    override fun ellipse(x: Float, y: Float, w: Float, h: Float) {
        if (!shouldDraw()) {
            return
        }

        if (shouldFill()) {
            canvas.drawOval(x, y, x + w, y + h, fillPaint)
        }

        if (shouldStroke()) {
            canvas.drawOval(x, y, x + w, y + h, strokePaint)
        }

    }

    override fun circle(x: Float, y: Float, diameter: Float) {
        ellipse(x - diameter / 2f, y - diameter / 2f, diameter, diameter)
    }

    override fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
        if (!shouldDraw()) {
            return
        }

        if (shouldFill()) {
            canvas.drawLine(x1, y1, x2, y2, fillPaint)
        }

        if (shouldStroke()) {
            canvas.drawLine(x1, y1, x2, y2, strokePaint)
        }
    }

    override fun lines(points: FloatArray) {
        if (!shouldDraw()) {
            return
        }

        if (shouldFill()) {
            canvas.drawLines(points, fillPaint)
        }

        if (shouldStroke()) {
            canvas.drawLines(points, strokePaint)
        }
    }

    override fun grid(
        spacing: Float,
        width: Float,
        height: Float
    ) {
        // Vertical
        var i = 0f
        while (i < width) {
            line(i, 0f, i, height)
            i += spacing
        }

        // Horizontal
        i = 0f
        while (i < height) {
            line(0f, i, width, i)
            i += spacing
        }
    }

    override fun point(x: Float, y: Float) {
        if (!shouldDraw()) {
            return
        }
        if (shouldFill()) {
            canvas.drawPoint(x, y, fillPaint)
        }

        if (shouldStroke()) {
            canvas.drawPoint(x, y, strokePaint)
        }
    }

    // TODO: Support different radius for each corner
    override fun rect(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        radius: Float
    ) {
        if (!shouldDraw()) {
            return
        }
        if (shouldFill()) {
            if (radius == 0f) {
                canvas.drawRect(x, y, x + w, y + h, fillPaint)
            } else {
                canvas.drawRoundRect(x, y, x + w, y + h, radius, radius, fillPaint)
            }
        }

        if (shouldStroke()) {
            if (radius == 0f) {
                canvas.drawRect(x, y, x + w, y + h, strokePaint)
            } else {
                canvas.drawRoundRect(x, y, x + w, y + h, radius, radius, strokePaint)
            }
        }
    }

    override fun square(
        x: Float,
        y: Float,
        size: Float,
        radius: Float
    ) {
        rect(x, y, size, size, radius)
    }

    override fun path(value: Path) {
        if (!shouldDraw()) {
            return
        }

        if (shouldFill()) {
            canvas.drawPath(value, fillPaint)
        }

        if (shouldStroke()) {
            canvas.drawPath(value, strokePaint)
        }
    }

    override fun triangle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        if (!shouldDraw()) {
            return
        }

        val trianglePath = Path()
        trianglePath.moveTo(x1, y1)
        trianglePath.lineTo(x2, y2)
        trianglePath.lineTo(x3, y3)
        trianglePath.lineTo(x1, y1)
        trianglePath.close()

        path(trianglePath)
    }

    // Transforms
    // TODO: Add the transforms
    override fun push() {
        canvas.save()
    }

    override fun pop() {
        canvas.restore()
    }

    override fun rotate(
        degrees: Float,
        originX: Float,
        originY: Float
    ) {
        canvas.rotate(degrees, originX, originY)
    }

    override fun scale(x: Float, y: Float) {
        canvas.scale(x, y)
    }

    override fun scale(x: Float, y: Float, pivotX: Float, pivotY: Float) {
        canvas.scale(x, y, pivotX, pivotY)
    }

    override fun translate(x: Float, y: Float) {
        canvas.translate(x, y)
    }

    // Images

    override fun loadImage(@DrawableRes id: Int, w: Int?, h: Int?): Bitmap {
        val drawable = ResourcesCompat.getDrawable(context.resources, id, null)!!
        return drawable.toBitmap(w ?: drawable.intrinsicWidth, h ?: drawable.intrinsicHeight)
    }

    override fun image(
        img: Bitmap,
        x: Float,
        y: Float,
        w: Float,
        h: Float
    ) {
        if (imageMode == ImageMode.Corner) {
            image(img, x, y, w, h, 0f, 0f)
        } else {
            image(img, x - w / 2f, y - h / 2f, w, h, 0f, 0f)
        }
    }

    override fun image(
        img: Bitmap,
        dx: Float,
        dy: Float,
        dw: Float,
        dh: Float,
        sx: Float,
        sy: Float,
        sw: Float,
        sh: Float
    ) {
        canvas.drawBitmap(
            img,
            Rect(sx.toInt(), sy.toInt(), sw.toInt(), sh.toInt()),
            Rect(dx.toInt(), dy.toInt(), (dx + dw).toInt(), (dy + dh).toInt()),
            fillPaint
        )
    }

    override fun imageMode(imageMode: ImageMode) {
        this.imageMode = imageMode
    }

    override fun textMode(textMode: TextMode) {
        this.textMode = textMode
    }

    private fun shouldDraw(): Boolean {
        return paintStyle != PaintStyle.None
    }

    private fun shouldFill(): Boolean {
        return paintStyle == PaintStyle.Fill || paintStyle == PaintStyle.FillAndStroke
    }

    private fun shouldStroke(): Boolean {
        return paintStyle == PaintStyle.Stroke || paintStyle == PaintStyle.FillAndStroke
    }

    // Masks

    override fun clip(path: Path) {
        canvas.clipPath(path)
    }

    override fun clipInverse(path: Path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.clipOutPath(path)
        } else {
            @Suppress("DEPRECATION")
            canvas.clipPath(path, Region.Op.DIFFERENCE)
        }
    }

    override fun mask(
        mask: Bitmap,
        tempBitmap: Bitmap,
        block: () -> Unit
    ): Bitmap {
        return canvas.getMaskedBitmap(
            mask,
            tempBitmap
        ) {
            val oldCanvas = canvas
            canvas = it
            block()
            canvas = oldCanvas
        }
    }

    // System

    override fun dp(size: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, size,
            context.resources.displayMetrics
        )
    }

    override fun sp(size: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, size,
            context.resources.displayMetrics
        )
    }
}
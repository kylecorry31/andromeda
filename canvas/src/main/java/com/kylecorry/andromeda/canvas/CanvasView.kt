package com.kylecorry.andromeda.canvas

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible

abstract class CanvasView : View {

    protected lateinit var canvas: Canvas
    protected lateinit var fillPaint: Paint
    protected lateinit var strokePaint: Paint
    protected var paintStyle = PaintStyle.Fill
    private var imageMode = ImageMode.Corner
    private var textMode = TextMode.Corner
    private val measurementRect = Rect()
    protected var runEveryCycle: Boolean = true

    private var isSetup = false

    protected var setupAfterVisible = false

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        this.canvas = canvas
        if (!isSetup && setupAfterVisible && !isVisible){
            return
        }
        if (!isSetup) {
            fillPaint = Paint()
            fillPaint.style = Paint.Style.FILL
            strokePaint = Paint()
            strokePaint.style = Paint.Style.STROKE
            smooth()
            strokeCap(StrokeCap.Round)
            strokeJoin(StrokeJoin.Miter)
            setup()
            isSetup = true
        }

        draw()
        if (runEveryCycle) {
            invalidate()
        }
    }

    abstract fun setup()

    abstract fun draw()


    // COLOR HELPERS
    // TODO: Handle stroke and fill

    fun background(@ColorInt color: Int) {
        canvas.drawColor(color)
    }

    fun clear() {
        background(Color.TRANSPARENT)
    }

    @ColorInt
    fun color(r: Int, g: Int = r, b: Int = g, a: Int? = null): Int {
        return if (a != null) {
            Color.argb(a, r, g, b)
        } else {
            Color.rgb(r, g, b)
        }
    }

    fun fill(@ColorInt color: Int) {
        paintStyle = if (shouldStroke()) {
            PaintStyle.FillAndStroke
        } else {
            PaintStyle.Fill
        }
        fillPaint.color = color
    }

    fun tint(@ColorInt color: Int) {
        fillPaint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        strokePaint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    fun noTint() {
        fillPaint.colorFilter = null
        strokePaint.colorFilter = null
    }

    fun stroke(@ColorInt color: Int) {
        paintStyle = if (shouldFill()) {
            PaintStyle.FillAndStroke
        } else {
            PaintStyle.Stroke
        }
        strokePaint.color = color
    }

    fun pathEffect(effect: PathEffect){
        strokePaint.pathEffect = effect
        fillPaint.pathEffect = effect
    }

    fun noPathEffect(){
        strokePaint.pathEffect = null
        fillPaint.pathEffect = null
    }

    fun noStroke() {
        paintStyle = if (shouldFill()) {
            PaintStyle.Fill
        } else {
            PaintStyle.None
        }
    }

    fun noFill() {
        paintStyle = if (shouldStroke()) {
            PaintStyle.Stroke
        } else {
            PaintStyle.None
        }
    }

    fun strokeWeight(pixels: Float) {
        strokePaint.strokeWidth = pixels
    }

    fun strokeCap(cap: StrokeCap) {
        strokePaint.strokeCap = when (cap) {
            StrokeCap.Round -> Paint.Cap.ROUND
            StrokeCap.Square -> Paint.Cap.SQUARE
            StrokeCap.Project -> Paint.Cap.BUTT
        }
    }

    fun strokeJoin(join: StrokeJoin) {
        strokePaint.strokeJoin = when (join) {
            StrokeJoin.Miter -> Paint.Join.MITER
            StrokeJoin.Bevel -> Paint.Join.BEVEL
            StrokeJoin.Round -> Paint.Join.ROUND
        }
    }

    fun opacity(value: Int){
        fillPaint.alpha = value
        strokePaint.alpha = value
    }

    fun erase() {
        // This may need the following to be called in setup: setLayerType(LAYER_TYPE_HARDWARE, null)
        fillPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        strokePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    fun noErase() {
        fillPaint.xfermode = null
        strokePaint.xfermode = null
    }

    fun smooth() {
        fillPaint.isAntiAlias = true
        strokePaint.isAntiAlias = true
    }

    fun noSmooth() {
        fillPaint.isAntiAlias = false
        strokePaint.isAntiAlias = false
    }

    // TEXT HELPERS
    fun textAlign(align: TextAlign) {
        val alignment = when (align) {
            TextAlign.Right -> Paint.Align.RIGHT
            TextAlign.Center -> Paint.Align.CENTER
            TextAlign.Left -> Paint.Align.LEFT
        }
        fillPaint.textAlign = alignment
        strokePaint.textAlign = alignment
    }

    fun textSize(pixels: Float) {
        fillPaint.textSize = pixels
        strokePaint.textSize = pixels
    }

    fun textStyle(style: TextStyle) {
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

    fun textWidth(text: String): Float {
        return textDimensions(text).first
    }

    fun textHeight(text: String): Float {
        return textDimensions(text).second
    }

    fun textDimensions(text: String): Pair<Float, Float> {
        // TODO: Factor in stroke
        fillPaint.getTextBounds(text, 0, text.length, measurementRect)
        return measurementRect.width().toFloat() to measurementRect.height().toFloat()
    }

    fun textAscent(): Float {
        // TODO: Factor in stroke
        return fillPaint.ascent()
    }

    fun textDescent(): Float {
        // TODO: Factor in stroke
        return fillPaint.descent()
    }

    fun text(str: String, x: Float, y: Float) {
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
    fun arc(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        start: Float,
        stop: Float,
        mode: ArcMode = ArcMode.Pie
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

    fun ellipse(x: Float, y: Float, w: Float, h: Float = w) {
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

    fun circle(x: Float, y: Float, diameter: Float) {
        ellipse(x - diameter / 2f, y - diameter / 2f, diameter, diameter)
    }

    fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
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

    fun grid(
        spacing: Float,
        width: Float = this.width.toFloat(),
        height: Float = this.height.toFloat()
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

    fun point(x: Float, y: Float) {
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
    fun rect(
        x: Float,
        y: Float,
        w: Float,
        h: Float = w,
        radius: Float = 0f
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

    fun square(
        x: Float,
        y: Float,
        size: Float,
        radius: Float = 0f
    ) {
        rect(x, y, size, size, radius)
    }

    fun path(value: Path){
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

    fun triangle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
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
    fun push() {
        canvas.save()
    }

    fun pop() {
        canvas.restore()
    }

    fun rotate(
        degrees: Float,
        originX: Float = width / 2f,
        originY: Float = height / 2f
    ) {
        canvas.rotate(degrees, originX, originY)
    }

    fun scale(x: Float, y: Float = x) {
        canvas.scale(x, y)
    }

    fun scale(x: Float, y: Float = x, pivotX: Float, pivotY: Float) {
        canvas.scale(x, y, pivotX, pivotY)
    }

    fun translate(x: Float, y: Float) {
        canvas.translate(x, y)
    }

    // Images

    fun loadImage(@DrawableRes id: Int, w: Int? = null, h: Int? = null): Bitmap {
        val drawable = ResourcesCompat.getDrawable(context.resources, id, null)!!
        return drawable.toBitmap(w ?: drawable.intrinsicWidth, h ?: drawable.intrinsicHeight)
    }

    fun image(
        img: Bitmap,
        x: Float,
        y: Float,
        w: Float = img.width.toFloat(),
        h: Float = img.height.toFloat()
    ) {
        if (imageMode == ImageMode.Corner) {
            image(img, x, y, w, h, 0f, 0f)
        } else {
            image(img, x - w / 2f, y - h / 2f, w, h, 0f, 0f)
        }
    }

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
    ) {
        canvas.drawBitmap(
            img,
            Rect(sx.toInt(), sy.toInt(), sw.toInt(), sh.toInt()),
            Rect(dx.toInt(), dy.toInt(), (dx + dw).toInt(), (dy + dh).toInt()),
            fillPaint
        )
    }

    fun imageMode(imageMode: ImageMode) {
        this.imageMode = imageMode
    }

    fun textMode(textMode: TextMode) {
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

    fun mask(
        mask: Bitmap,
        tempBitmap: Bitmap = Bitmap.createBitmap(
            mask.width,
            mask.height,
            Bitmap.Config.ARGB_8888
        ),
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

    fun dp(size: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, size,
            context.resources.displayMetrics
        )
    }

    fun sp(size: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, size,
            context.resources.displayMetrics
        )
    }


    enum class ArcMode {
        Pie,
        Open
    }

    enum class StrokeCap {
        Round,
        Square,
        Project
    }

    enum class StrokeJoin {
        Miter,
        Bevel,
        Round
    }

    enum class TextAlign {
        Right, Center, Left
    }

    enum class TextStyle {
        Normal,
        Italic,
        Bold,
        BoldItalic
    }

    enum class PaintStyle {
        Fill,
        Stroke,
        FillAndStroke,
        None
    }

    enum class ImageMode {
        Corner,
        Center
    }

    enum class TextMode {
        Corner,
        Center
    }

}
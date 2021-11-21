package com.kylecorry.andromeda.canvas

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible

abstract class CanvasView : View, ICanvasDrawer {

    protected lateinit var drawer: ICanvasDrawer
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
        if (!isSetup && setupAfterVisible && !isVisible) {
            return
        }

        if (!isSetup) {
            drawer = CanvasDrawer(context, canvas)
            setup()
            isSetup = true
        }

        drawer.canvas = canvas

        draw()
        if (runEveryCycle) {
            invalidate()
        }
    }

    abstract fun setup()

    abstract fun draw()


    // COLOR HELPERS
    override fun background(@ColorInt color: Int) {
        drawer.background(color)
    }

    override fun clear() {
        drawer.clear()
    }

    @ColorInt
    override fun color(r: Int, g: Int, b: Int, a: Int?): Int {
        return drawer.color(r, g, b, a)
    }

    override fun fill(@ColorInt color: Int) {
        drawer.fill(color)
    }

    override fun tint(@ColorInt color: Int) {
        drawer.tint(color)
    }

    override fun noTint() {
        drawer.noTint()
    }

    override fun stroke(@ColorInt color: Int) {
        drawer.stroke(color)
    }

    override fun pathEffect(effect: PathEffect) {
        drawer.pathEffect(effect)
    }

    override fun noPathEffect() {
        drawer.noPathEffect()
    }

    override fun noStroke() {
        drawer.noStroke()
    }

    override fun noFill() {
        drawer.noFill()
    }

    override fun strokeWeight(pixels: Float) {
        drawer.strokeWeight(pixels)
    }

    override fun strokeCap(cap: StrokeCap) {
        drawer.strokeCap(cap)
    }

    override fun strokeJoin(join: StrokeJoin) {
        drawer.strokeJoin(join)
    }

    override fun opacity(value: Int) {
        drawer.opacity(value)
    }

    override fun erase() {
        drawer.erase()
    }

    override fun noErase() {
        drawer.noErase()
    }

    override fun smooth() {
        drawer.smooth()
    }

    override fun noSmooth() {
        drawer.noSmooth()
    }

    // TEXT HELPERS
    override fun textAlign(align: TextAlign) {
        drawer.textAlign(align)
    }

    override fun textSize(pixels: Float) {
        drawer.textSize(pixels)
    }

    override fun textStyle(style: TextStyle) {
        drawer.textStyle(style)
    }

    override fun textWidth(text: String): Float {
        return drawer.textWidth(text)
    }

    override fun textHeight(text: String): Float {
        return drawer.textHeight(text)
    }

    override fun textDimensions(text: String): Pair<Float, Float> {
        return drawer.textDimensions(text)
    }

    override fun pathWidth(path: Path): Float {
        return drawer.pathWidth(path)
    }

    override fun pathHeight(path: Path): Float {
        return drawer.pathHeight(path)
    }

    override fun pathDimensions(path: Path): Pair<Float, Float> {
        return drawer.pathDimensions(path)
    }

    override fun textAscent(): Float {
        return drawer.textAscent()
    }

    override fun textDescent(): Float {
        return drawer.textDescent()
    }

    override fun text(str: String, x: Float, y: Float) {
        drawer.text(str, x, y)
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
        drawer.arc(x, y, w, h, start, stop, mode)
    }

    override fun ellipse(x: Float, y: Float, w: Float, h: Float) {
        drawer.ellipse(x, y, w, h)
    }

    override fun circle(x: Float, y: Float, diameter: Float) {
        drawer.circle(x, y, diameter)
    }

    override fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
        drawer.line(x1, y1, x2, y2)
    }

    override fun lines(points: FloatArray) {
        drawer.lines(points)
    }

    override fun grid(
        spacing: Float,
        width: Float,
        height: Float
    ) {
        drawer.grid(spacing, width, height)
    }

    override fun point(x: Float, y: Float) {
        drawer.point(x, y)
    }

    // TODO: Support different radius for each corner
    override fun rect(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        radius: Float
    ) {
        drawer.rect(x, y, w, h, radius)
    }

    override fun square(
        x: Float,
        y: Float,
        size: Float,
        radius: Float
    ) {
        drawer.square(x, y, size, radius)
    }

    override fun path(value: Path) {
        drawer.path(value)
    }

    override fun triangle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        drawer.triangle(x1, y1, x2, y2, x3, y3)
    }

    // Transforms
    override fun push() {
        drawer.push()
    }

    override fun pop() {
        drawer.pop()
    }

    override fun rotate(
        degrees: Float,
        originX: Float,
        originY: Float
    ) {
        drawer.rotate(degrees, originX, originY)
    }

    override fun scale(x: Float, y: Float) {
        drawer.scale(x, y)
    }

    override fun scale(x: Float, y: Float, pivotX: Float, pivotY: Float) {
        drawer.scale(x, y, pivotX, pivotY)
    }

    override fun translate(x: Float, y: Float) {
        drawer.translate(x, y)
    }

    // Images

    override fun loadImage(@DrawableRes id: Int, w: Int?, h: Int?): Bitmap {
        return drawer.loadImage(id, w, h)
    }

    override fun image(
        img: Bitmap,
        x: Float,
        y: Float,
        w: Float,
        h: Float
    ) {
        drawer.image(img, x, y, w, h)
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
        drawer.image(img, dx, dy, dw, dh, sx, sy, sw, sh)
    }

    override fun imageMode(imageMode: ImageMode) {
        drawer.imageMode(imageMode)
    }

    override fun textMode(textMode: TextMode) {
        drawer.textMode(textMode)
    }

    // Masks

    override fun clip(path: Path) {
        drawer.clip(path)
    }

    override fun clipInverse(path: Path) {
        drawer.clipInverse(path)
    }

    override fun mask(
        mask: Bitmap,
        tempBitmap: Bitmap,
        block: () -> Unit
    ): Bitmap {
        return drawer.mask(mask, tempBitmap, block)
    }

    // System

    override fun dp(size: Float): Float {
        return drawer.dp(size)
    }

    override fun sp(size: Float): Float {
        return drawer.sp(size)
    }

}
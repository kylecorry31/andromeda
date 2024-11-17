package com.kylecorry.andromeda.core.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.drawToBitmap
import androidx.core.view.setPadding

object Views {

    fun scroll(
        child: View,
        width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
        height: Int = ViewGroup.LayoutParams.MATCH_PARENT,
        padding: Int = 0
    ): View {
        val scrollView = ScrollView(child.context)
        scrollView.layoutParams = ViewGroup.LayoutParams(width, height)
        scrollView.setPadding(padding)
        scrollView.addView(child)
        return scrollView
    }

    fun linear(
        views: List<View>,
        width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        orientation: Int = LinearLayout.VERTICAL,
        padding: Int = 0
    ): View {
        val layout = LinearLayout(views.first().context)
        layout.layoutParams = ViewGroup.LayoutParams(width, height)
        layout.orientation = orientation
        layout.setPadding(padding, padding, padding, padding)

        views.forEach { view ->
            layout.addView(view)
        }

        return layout
    }

    fun text(
        context: Context,
        text: CharSequence?,
        id: Int? = null,
        width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
    ): View {
        return TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(width, height)
            if (id != null) {
                this.id = id
            }
            this.text = text
        }
    }

    fun renderViewAsBitmap(view: View): Bitmap {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        return view.drawToBitmap()
    }

    fun renderViewAsBitmap(view: View, width: Int, height: Int): Bitmap {
        view.layoutParams = ViewGroup.LayoutParams(
            width,
            height
        )
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        return view.drawToBitmap()
    }

    fun renderViewToCanvas(view: View, canvas: Canvas) {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(canvas.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(canvas.height, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.draw(canvas)
    }
}
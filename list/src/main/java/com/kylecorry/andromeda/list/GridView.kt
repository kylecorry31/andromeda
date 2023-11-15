package com.kylecorry.andromeda.list

import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GridView<T>(
    view: RecyclerView,
    @LayoutRes itemLayoutId: Int,
    spanCount: Int,
    isVertical: Boolean = true,
    reverseLayout: Boolean = false,
    getId: (T) -> Long? = { null },
    onViewBind: (View, T) -> Unit
) {

    private val list: ListView<T> = ListView(view, itemLayoutId, getId, onViewBind)
    private var spans = listOf<Int>()

    init {
        val layoutManager = GridLayoutManager(
            view.context,
            spanCount,
            if (isVertical) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL,
            reverseLayout
        )
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return spans.getOrElse(position) { 1 }
            }
        }
        list.setLayoutManager(layoutManager)
    }

    fun setData(data: List<T>) {
        setSpannedData(data, List(data.size) { 1 })
    }

    fun setSpannedData(data: List<SpannedItem<T>>) {
        setSpannedData(data.map { it.item }, data.map { it.span })
    }

    fun setSpannedData(data: List<T>, spans: List<Int>) {
        list.setData(data)
        this.spans = spans
    }

    fun addLineSeparator() {
        list.addLineSeparator()
    }

    fun removeLineSeparator() {
        list.removeLineSeparator()
    }

    fun scrollToPosition(position: Int, smooth: Boolean = true) {
        list.scrollToPosition(position, smooth)
    }

    class SpannedItem<T>(val item: T, val span: Int)
}
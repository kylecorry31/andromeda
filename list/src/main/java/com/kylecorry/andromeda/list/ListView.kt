package com.kylecorry.andromeda.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListView<T>(
    private val view: RecyclerView,
    @LayoutRes private val itemLayoutId: Int,
    private val getId: (T) -> Long? = { null },
    private val onViewBind: (View, T) -> Unit
) {

    private val adapter: Adapter
    private val layoutInflater: LayoutInflater
    private var layoutManager: LinearLayoutManager

    private val separator: DividerItemDecoration

    init {
        layoutManager = LinearLayoutManager(view.context)
        view.layoutManager = layoutManager

        separator = DividerItemDecoration(
            view.context,
            layoutManager.orientation
        )

        layoutInflater = LayoutInflater.from(view.context)

        adapter = Adapter(listOf())
        view.adapter = adapter
    }

    fun setLayoutManager(manager: LinearLayoutManager) {
        layoutManager = manager
        view.layoutManager = layoutManager
        separator.setOrientation(layoutManager.orientation)
    }

    fun setData(data: List<T>) {
        adapter.data = data
    }

    fun addLineSeparator() {
        view.addItemDecoration(separator)
    }

    fun removeLineSeparator() {
        view.removeItemDecoration(separator)
    }

    fun scrollToPosition(position: Int, smooth: Boolean = true) {
        if (smooth) {
            view.smoothScrollToPosition(position)
        } else {
            view.scrollToPosition(position)
        }
    }

    inner class Adapter(mData: List<T>) : RecyclerView.Adapter<Holder>() {

        var data: List<T> = mData
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = layoutInflater.inflate(itemLayoutId, parent, false)
            return Holder(view)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun getItemId(position: Int): Long {
            return getId(data[position]) ?: super.getItemId(position)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(data[position])
        }

    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(detail: T) {
            onViewBind(itemView, detail)
        }
    }


}
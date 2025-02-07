package com.kylecorry.andromeda.views.list

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.kylecorry.andromeda.core.system.Resources
import com.kylecorry.andromeda.core.ui.Colors
import com.kylecorry.andromeda.list.ListView
import com.kylecorry.andromeda.pickers.Pickers
import com.kylecorry.andromeda.views.R
import com.kylecorry.andromeda.views.badge.Badge
import com.kylecorry.andromeda.views.databinding.AndromedaViewListItemBinding

class AndromedaListView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {

    var items: List<ListItem>? = null
        private set

    private val list =
        ListView(this, R.layout.andromeda_view_list_item) { view: View, listItem: ListItem ->
            val binding = AndromedaViewListItemBinding.bind(view)
            binding.title.text = listItem.title

            binding.title.maxLines = listItem.titleMaxLines

            if (listItem.checkbox != null) {
                binding.checkbox.isChecked = listItem.checkbox.checked
                binding.checkbox.setOnClickListener { listItem.checkbox.onClick() }
                binding.checkbox.isVisible = true
            } else {
                binding.checkbox.isVisible = false
            }

            binding.description.maxLines = listItem.subtitleMaxLines

            binding.description.text = listItem.subtitle
            binding.description.isVisible = listItem.subtitle != null

            if (listItem.tags.isNotEmpty()) {
                binding.tags.isVisible = true
                val margin = Resources.dp(context, 8f).toInt()
                val tagViews = listItem.tags.map {
                    Badge(view.context, null).apply {
                        val foregroundColor =
                            Colors.mostContrastingColor(Color.WHITE, Color.BLACK, it.color)
                        statusImage.isVisible = it.icon != null
                        it.icon?.let {
                            it.apply(statusImage)
                            Colors.setImageColor(statusImage, foregroundColor)
                        }
                        setStatusText(it.text)
                        statusText.setTextColor(foregroundColor)
                        setBackgroundTint(it.color)
                        layoutParams = FlexboxLayout.LayoutParams(
                            FlexboxLayout.LayoutParams.WRAP_CONTENT,
                            FlexboxLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 0, margin, margin)
                        }
                    }
                }
                binding.tags.removeAllViews()
                tagViews.forEach {
                    binding.tags.addView(it)
                }
            } else {
                binding.tags.isVisible = false
            }

            binding.trailingText.isVisible = listItem.trailingText != null
            binding.trailingText.text = listItem.trailingText
            binding.icon.isVisible = listItem.icon != null
            listItem.icon?.apply(binding.icon)
            binding.trailingIconBtn.isVisible = listItem.trailingIcon != null
            listItem.trailingIcon?.apply(binding.trailingIconBtn)
            if (listItem.menu.isNotEmpty()) {
                binding.menuBtn.isVisible = true
                binding.menuBtn.setOnClickListener {
                    Pickers.menu(it, listItem.menu.map { it.text }) { idx ->
                        listItem.menu[idx].action()
                        true
                    }
                }
            } else {
                binding.menuBtn.isVisible = false
            }

            binding.root.setOnClickListener { listItem.action() }
            binding.root.setOnLongClickListener {
                listItem.longClickAction()
                true
            }

            val dataViews = listOf(
                binding.data1,
                binding.data2,
                binding.data3
            )

            binding.data.justifyContent = listItem.dataAlignment.justifyContent
            binding.data.alignItems = listItem.dataAlignment.alignItems
            binding.data.alignContent = listItem.dataAlignment.alignContent

            for (i in dataViews.indices) {
                // TODO: Allow more than 3 data points
                if (listItem.data.size > i) {
                    dataViews[i].isVisible = true
                    val data = listItem.data[i]
                    dataViews[i].text = data.text
                    if (data.icon == null) {
                        dataViews[i].setCompoundDrawables(null, null, null, null)
                    } else {
                        data.icon.apply(dataViews[i])
                    }

                    // Set the layout properties for grow, shrink, and basis - create it if it doesn't exist
                    val layoutParams = dataViews[i].layoutParams as? FlexboxLayout.LayoutParams
                        ?: FlexboxLayout.LayoutParams(
                            FlexboxLayout.LayoutParams.WRAP_CONTENT,
                            FlexboxLayout.LayoutParams.WRAP_CONTENT
                        )
                    layoutParams.flexGrow = data.grow
                    layoutParams.flexShrink = data.shrink
                    layoutParams.flexBasisPercent = data.basisPercentage
                    layoutParams.alignSelf = data.alignment
                    dataViews[i].layoutParams = layoutParams
                } else {
                    dataViews[i].isVisible = false
                }
            }

            binding.data.isVisible = listItem.data.isNotEmpty()

        }

    var emptyView: View? = null

    fun setItems(items: List<ListItem>) {
        // TODO: Be smart about how the list gets updated
        this.items = items
        list.setData(items)
        emptyView?.isVisible = items.isEmpty()
    }

    fun <T> setItems(items: List<T>, mapper: ListItemMapper<T>) {
        setItems(items.map { mapper.map(it) })
    }

    fun scrollToPosition(position: Int, smooth: Boolean = true) {
        list.scrollToPosition(position, smooth)
    }

    init {
        list.addLineSeparator()
    }
}
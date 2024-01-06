package com.kylecorry.andromeda.views.list

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.kylecorry.andromeda.core.ui.Colors
import com.kylecorry.andromeda.list.ListView
import com.kylecorry.andromeda.pickers.Pickers
import com.kylecorry.andromeda.views.R
import com.kylecorry.andromeda.views.databinding.AndromedaViewListItemBinding

class AndromedaListView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {

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
                // TODO: Allow multiple
                val tag = listItem.tags.first()
                binding.tag.isVisible = true
                val foregroundColor =
                    Colors.mostContrastingColor(Color.WHITE, Color.BLACK, tag.color)
                binding.tag.statusImage.isVisible = tag.icon != null
                tag.icon?.let {
                    it.apply(binding.tag.statusImage)
                    Colors.setImageColor(binding.tag.statusImage, foregroundColor)
                }
                binding.tag.setStatusText(tag.text)
                binding.tag.statusText.setTextColor(foregroundColor)
                binding.tag.setBackgroundTint(tag.color)
            } else {
                binding.tag.isVisible = false
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
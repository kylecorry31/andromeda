package com.kylecorry.andromeda.views.list

import android.graphics.Bitmap
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.lifecycle.LifecycleOwner
import com.google.android.flexbox.AlignSelf
import com.kylecorry.andromeda.core.system.Resources
import com.kylecorry.andromeda.core.tryOrLog
import com.kylecorry.andromeda.core.ui.Colors
import com.kylecorry.andromeda.core.ui.setCompoundDrawables
import com.kylecorry.andromeda.views.image.AsyncImageView
import kotlin.math.roundToInt

data class ListItem(
    val id: Long,
    val title: CharSequence,
    val subtitle: CharSequence? = null,
    val titleMaxLines: Int = Int.MAX_VALUE,
    val subtitleMaxLines: Int = Int.MAX_VALUE,
    val icon: ListIcon? = null,
    val checkbox: ListItemCheckbox? = null,
    val tags: List<ListItemTag> = emptyList(),
    val data: List<ListItemData> = emptyList(),
    val dataAlignment: ListItemDataAlignment = ListItemDataAlignment(),
    val trailingText: CharSequence? = null,
    val trailingIcon: ListIcon? = null,
    val menu: List<ListMenuItem> = emptyList(),
    val longClickAction: () -> Unit = {},
    val action: () -> Unit = {}
)

data class ListMenuItem(val text: String, val action: () -> Unit)

data class ListItemTag(val text: String, val icon: ListIcon?, @ColorInt val color: Int)

data class ListItemData(
    val text: CharSequence,
    val icon: ListIcon?,
    val grow: Float = 0f,
    val shrink: Float = 1f,
    val basisPercentage: Float = -1f,
    @AlignSelf val alignment: Int = AlignSelf.AUTO
)

data class ListItemCheckbox(val checked: Boolean, val onClick: () -> Unit)

interface ListIcon {
    fun apply(image: ImageView)
    fun apply(text: TextView)
}

data class ResourceListIcon(
    @DrawableRes val id: Int,
    @ColorInt val tint: Int? = null,
    @DrawableRes val backgroundId: Int? = null,
    @ColorInt val backgroundTint: Int? = null,
    val size: Float = 24f,
    val foregroundSize: Float = size,
    val clipToBackground: Boolean = false,
    val scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_CENTER,
    val onClick: (() -> Unit)? = null
) : ListIcon {
    override fun apply(image: ImageView) {
        image.isVisible = true
        image.setImageResource(id)
        if (image is AsyncImageView) {
            image.recycleLastBitmap(false)
        }
        Colors.setImageColor(image, tint)

        image.scaleType = scaleType
        tryOrLog {
            image.layoutParams.width = Resources.dp(image.context, size).toInt()
            image.layoutParams.height = Resources.dp(image.context, size).toInt()
        }

        if (backgroundId != null) {
            image.setBackgroundResource(backgroundId)
            backgroundTint?.let {
                Colors.setImageColor(image.background, it)
            }
        } else {
            image.background = null
        }

        if (clipToBackground) {
            image.outlineProvider = ViewOutlineProvider.BACKGROUND
            image.clipToOutline = true
        } else {
            image.clipToOutline = false
        }

        val padding = Resources.dp(image.context, (size - foregroundSize)) / 2f
        image.setPadding(padding.roundToInt())

        image.requestLayout()
        if (onClick == null) {
            image.setOnClickListener(null)
        } else {
            image.setOnClickListener { onClick.invoke() }
        }
    }

    override fun apply(text: TextView) {
        text.setCompoundDrawables(
            Resources.dp(text.context, 12f).toInt(),
            left = id
        )
        Colors.setImageColor(text, tint)
        // TODO: Possibly apply background color
    }
}

data class AsyncListIcon(
    val lifecycleOwner: LifecycleOwner,
    val bitmapLoader: suspend () -> Bitmap,
    @ColorInt val tint: Int? = null,
    val size: Float = 24f,
    val scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_CENTER,
    val clearOnPause: Boolean = false,
    val onClick: (() -> Unit)? = null
) : ListIcon {
    override fun apply(image: ImageView) {
        if (image !is AsyncImageView) {
            return
        }

        image.clearOnPause = clearOnPause
        image.isVisible = true
        image.setImageBitmap(lifecycleOwner, bitmapLoader)
        Colors.setImageColor(image, tint)

        image.scaleType = scaleType
        tryOrLog {
            image.layoutParams.width = Resources.dp(image.context, size).toInt()
            image.layoutParams.height = Resources.dp(image.context, size).toInt()
        }

        image.background = null
        image.clipToOutline = false

        image.setPadding(0)

        image.requestLayout()
        if (onClick == null) {
            image.setOnClickListener(null)
        } else {
            image.setOnClickListener { onClick.invoke() }
        }
    }

    override fun apply(text: TextView) {
        // Does not apply to textview
    }
}
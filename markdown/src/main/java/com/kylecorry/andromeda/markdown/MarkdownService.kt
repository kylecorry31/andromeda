package com.kylecorry.andromeda.markdown

import android.content.Context
import android.graphics.Rect
import android.text.Spanned
import android.widget.TextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.core.spans.LastLineSpacingSpan
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.ImageSize
import io.noties.markwon.image.ImageSizeResolverDef
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.file.FileSchemeHandler
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.simple.ext.SimpleExtPlugin
import org.commonmark.node.ListItem

class MarkdownService(
    private val context: Context,
    private val autoLink: Boolean = true,
    private val extensions: List<MarkdownExtension> = emptyList()
) {
    private val markwon by lazy {
        val builder = Markwon.builder(context)
        builder.usePlugin(object : AbstractMarkwonPlugin() {

            override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                super.configureConfiguration(builder)
                builder.imageSizeResolver(FitWidthImageSizeResolver())
            }

            override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                super.configureSpansFactory(builder)
                builder.appendFactory(
                    ListItem::class.java
                ) { _, _ -> LastLineSpacingSpan(16) }
            }
        })

        builder.usePlugin(ImagesPlugin.create {
            it.addSchemeHandler(FileSchemeHandler.createWithAssets(context))
        })

        builder.usePlugin(SimpleExtPlugin.create {
            extensions.forEach { ext ->
                it.addExtension(ext.length, ext.openingCharacter, ext.closingCharacter) { _, _ ->
                    ext.spanProducer()
                }
            }
        })

        if (autoLink) {
            builder.usePlugin(LinkifyPlugin.create(true))
        }

        builder.build()
    }

    fun setMarkdown(view: TextView, markdown: String) {
        markwon.setMarkdown(view, markdown)
    }

    fun setParsedMarkdown(view: TextView, markdown: Spanned) {
        markwon.setParsedMarkdown(view, markdown)
    }

    fun toMarkdown(markdown: String): Spanned {
        return markwon.toMarkdown(markdown)
    }

    private class FitWidthImageSizeResolver : ImageSizeResolverDef() {
        override fun resolveImageSize(drawable: AsyncDrawable): Rect {
            return resolveImageSize(
                ImageSize(
                    ImageSize.Dimension(100F, UNIT_PERCENT),
                    null
                ),
                // important detail - `drawable.result` bounds must be used
                drawable.result.bounds,
                drawable.lastKnownCanvasWidth,
                drawable.lastKnowTextSize
            )
        }
    }

}
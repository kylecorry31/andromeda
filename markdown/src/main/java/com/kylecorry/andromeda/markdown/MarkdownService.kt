package com.kylecorry.andromeda.markdown

import android.content.Context
import android.text.Spanned
import android.widget.TextView
import androidx.core.text.util.LinkifyCompat
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.core.spans.LastLineSpacingSpan
import io.noties.markwon.linkify.LinkifyPlugin
import org.commonmark.node.ListItem

class MarkdownService(private val context: Context, private val autoLink: Boolean = true) {
    private val markwon by lazy {
        val builder = Markwon.builder(context)
        builder.usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                super.configureSpansFactory(builder)
                builder.appendFactory(
                    ListItem::class.java
                ) { _, _ -> LastLineSpacingSpan(16) }
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

    fun toMarkdown(markdown: String): Spanned {
        return markwon.toMarkdown(markdown)
    }

}
package com.kylecorry.andromeda.pickers

import android.content.Context
import android.view.View
import androidx.annotation.MenuRes
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object CoroutinePickers {

    suspend fun text(
        context: Context,
        title: CharSequence,
        description: CharSequence? = null,
        default: String? = null,
        hint: CharSequence? = null,
        okText: CharSequence? = context.getString(android.R.string.ok),
        cancelText: CharSequence? = context.getString(android.R.string.cancel)
    ) = suspendCoroutine<String?> { cont ->
        Pickers.text(
            context,
            title,
            description,
            default,
            hint,
            okText,
            cancelText
        ) {
            cont.resume(it)
        }
    }

    suspend fun time(
        context: Context,
        use24Hours: Boolean,
        default: LocalTime = LocalTime.now()
    ) = suspendCoroutine<LocalTime?> { cont ->
        Pickers.time(
            context,
            use24Hours,
            default
        ) {
            cont.resume(it)
        }
    }

    suspend fun date(
        context: Context,
        default: LocalDate = LocalDate.now()
    ) = suspendCoroutine<LocalDate?> { cont ->
        Pickers.date(
            context,
            default
        ) {
            cont.resume(it)
        }
    }

    suspend fun datetime(
        context: Context,
        use24Hours: Boolean,
        default: LocalDateTime = LocalDateTime.now()
    ) = suspendCoroutine<LocalDateTime?> { cont ->
        Pickers.datetime(context, use24Hours, default) {
            cont.resume(it)
        }
    }

    suspend fun menu(anchorView: View, @MenuRes menu: Int) = suspendCoroutine<Int> { cont ->
        Pickers.menu(anchorView, menu) {
            cont.resume(it)
            true
        }
    }

    suspend fun menu(anchorView: View, items: List<String?>) = suspendCoroutine<Int> { cont ->
        Pickers.menu(anchorView, items) {
            cont.resume(it)
            true
        }
    }

    suspend fun number(
        context: Context,
        title: CharSequence,
        description: CharSequence? = null,
        default: Number? = null,
        allowDecimals: Boolean = true,
        allowNegative: Boolean = false,
        hint: CharSequence? = null,
        okText: CharSequence? = context.getString(android.R.string.ok),
        cancelText: CharSequence? = context.getString(android.R.string.cancel)
    ) = suspendCoroutine<Number?> { cont ->
        Pickers.number(
            context,
            title,
            description,
            default,
            allowDecimals,
            allowNegative,
            hint,
            okText,
            cancelText
        ) {
            cont.resume(it)
        }
    }

    suspend fun item(
        context: Context,
        title: CharSequence,
        items: List<String>,
        defaultSelectedIndex: Int = -1,
        okText: CharSequence? = context.getString(android.R.string.ok),
        cancelText: CharSequence? = context.getString(android.R.string.cancel)
    ) = suspendCoroutine<Int?> { cont ->
        Pickers.item(context, title, items, defaultSelectedIndex, okText, cancelText) {
            cont.resume(it)
        }
    }

    suspend fun items(
        context: Context,
        title: CharSequence,
        items: List<String>,
        defaultSelectedIndices: List<Int> = listOf(),
        okText: CharSequence? = context.getString(android.R.string.ok),
        cancelText: CharSequence? = context.getString(android.R.string.cancel)
    ) = suspendCoroutine<List<Int>?> { cont ->
        Pickers.items(context, title, items, defaultSelectedIndices, okText, cancelText) {
            cont.resume(it)
        }
    }

}
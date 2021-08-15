package com.kylecorry.andromeda.pickers

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.View
import android.widget.PopupMenu
import android.widget.TimePicker
import androidx.annotation.MenuRes
import java.time.LocalDate
import java.time.LocalTime

object Pickers {

    fun time(
        context: Context,
        use24Hours: Boolean,
        default: LocalTime = LocalTime.now(),
        onTimePick: (time: LocalTime?) -> Unit
    ) {
        val timePickerDialog = TimePickerDialog(
            context,
            { timePicker: TimePicker, hour: Int, minute: Int ->
                val time = LocalTime.of(hour, minute)
                onTimePick.invoke(time)
            },
            default.hour,
            default.minute,
            use24Hours
        )
        timePickerDialog.setOnCancelListener {
            onTimePick.invoke(null)
        }
        timePickerDialog.show()
    }

    fun date(
        context: Context,
        default: LocalDate = LocalDate.now(),
        onDatePick: (date: LocalDate?) -> Unit
    ) {
        val datePickerDialog = DatePickerDialog(
            context,
            { view, year, month, dayOfMonth ->
                val date = LocalDate.of(year, month + 1, dayOfMonth)
                onDatePick.invoke(date)
            },
            default.year,
            default.monthValue - 1,
            default.dayOfMonth
        )
        datePickerDialog.setOnCancelListener {
            onDatePick.invoke(null)
        }
        datePickerDialog.show()
    }

    fun menu(anchorView: View, @MenuRes menu: Int, onSelection: (itemId: Int) -> Boolean) {
        val popup = PopupMenu(anchorView.context, anchorView)
        val inflater = popup.menuInflater
        inflater.inflate(menu, popup.menu)
        popup.setOnMenuItemClickListener {
            onSelection.invoke(it.itemId)
        }
        popup.show()
    }

    // TODO: Add item, items, duration, distance, location

}
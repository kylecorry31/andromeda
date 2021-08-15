package com.kylecorry.andromeda.pickers

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.TimePicker
import androidx.annotation.MenuRes
import androidx.appcompat.app.AlertDialog
import com.kylecorry.andromeda.alerts.Alerts
import com.kylecorry.andromeda.core.math.toDoubleCompat
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

    fun text(
        context: Context,
        title: CharSequence,
        description: CharSequence? = null,
        default: String? = null,
        hint: CharSequence? = null,
        okText: CharSequence? = context.getString(android.R.string.ok),
        cancelText: CharSequence? = context.getString(android.R.string.cancel),
        onTextEnter: (text: String?) -> Unit
    ) {
        val layout = FrameLayout(context)
        val editTextView = EditText(context)
        editTextView.setText(default)
        editTextView.hint = hint
        layout.setPadding(64, 0, 64, 0)
        layout.addView(editTextView)

        Alerts.dialog(context, title, description, contentView = layout, okText, cancelText){ cancelled ->
           if (!cancelled){
               onTextEnter.invoke(editTextView.text.toString())
           } else {
               onTextEnter.invoke(null)
           }
        }
    }

    fun number(
        context: Context,
        title: CharSequence,
        description: CharSequence? = null,
        default: Number? = null,
        allowDecimals: Boolean = true,
        allowNegative: Boolean = false,
        hint: CharSequence? = null,
        okText: CharSequence? = context.getString(android.R.string.ok),
        cancelText: CharSequence? = context.getString(android.R.string.cancel),
        onNumberEnter: (number: Number?) -> Unit
    ) {
        val layout = FrameLayout(context)
        val editTextView = EditText(context)
        if (default != null) {
            editTextView.setText(default.toString())
        }
        editTextView.inputType = InputType.TYPE_CLASS_NUMBER or
                (if (allowDecimals) InputType.TYPE_NUMBER_FLAG_DECIMAL else 0) or
                (if (allowNegative) InputType.TYPE_NUMBER_FLAG_SIGNED else 0)
        editTextView.hint = hint
        layout.setPadding(64, 0, 64, 0)
        layout.addView(editTextView)

        Alerts.dialog(context, title, description, contentView = layout, okText, cancelText){ cancelled ->
            if (!cancelled){
                onNumberEnter.invoke(editTextView.text.toString().toDoubleCompat())
            } else {
                onNumberEnter.invoke(null)
            }
        }
    }

    // TODO: Add item, items, duration, distance, location

}
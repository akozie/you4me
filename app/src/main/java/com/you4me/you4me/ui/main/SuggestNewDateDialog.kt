package com.you4me.you4me.ui.main

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.you4me.you4me.R
import java.util.*

class SuggestNewDateDialog(private val onDateSelected: (String, String) -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.dialog_suggest_new_date, null)

        val btnDatePicker: ImageButton = view.findViewById(R.id.btnDatePicker)
        val btnTimePicker: ImageButton = view.findViewById(R.id.btnTimePicker)
        val btnCancel: Button = view.findViewById(R.id.btnCancel)
        val btnDone: Button = view.findViewById(R.id.btnDone)
        val tvPreviousDate: TextView = view.findViewById(R.id.tvPreviousDate)
        val tvSuggestNewDate: TextView = view.findViewById(R.id.tvSuggestNewDate)

        val calendar = Calendar.getInstance()
        var selectedDate = "13 Dec, 2024"
        var selectedTime = "7PM"

        // Date Picker
        btnDatePicker.setOnClickListener {
            val datePicker =
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        selectedDate = "$dayOfMonth ${getMonthName(month)}, $year"
                        tvSuggestNewDate.text = "Suggest New Date: $selectedDate"
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                )
            datePicker.show()
        }

        // Time Picker
        btnTimePicker.setOnClickListener {
            val timePicker =
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val isPM = hourOfDay >= 12
                        val hour =
                            if (hourOfDay > 12) {
                                hourOfDay - 12
                            } else if (hourOfDay == 0) {
                                12
                            } else {
                                hourOfDay
                            }
                        selectedTime = "$hour:${minute.toString().padStart(2, '0')} ${if (isPM) "PM" else "AM"}"
                        tvSuggestNewDate.text = "Suggest New Date: $selectedDate, $selectedTime"
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false,
                )
            timePicker.show()
        }

        // Cancel Button
        btnCancel.setOnClickListener {
            dismiss()
        }

        // Done Button
        btnDone.setOnClickListener {
            onDateSelected(selectedDate, selectedTime)
            dismiss()
        }

        return AlertDialog.Builder(context)
            .setView(view)
            .create()
    }

    private fun getMonthName(month: Int): String {
        return arrayOf(
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Oct",
            "Nov",
            "Dec",
        )[month]
    }
}

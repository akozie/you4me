package com.you4me.you4me.ui.main

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.you4me.you4me.R
import com.you4me.you4me.utils.Utils
import java.util.*

class SuggestNewDateDialog(
    private var selectedDate: String,
    private var selectedTime: String,
    private val onDateSelected: (String, String) -> Unit,
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.dialog_suggest_new_date, null)

        val btnDatePicker: TextView = view.findViewById(R.id.btnDatePicker)
        val btnTimePicker: TextView = view.findViewById(R.id.btnTimePicker)
        val btnCancel: Button = view.findViewById(R.id.btnCancel)
        val btnDone: Button = view.findViewById(R.id.btnDone)
        val tvPreviousDate: TextView = view.findViewById(R.id.tvPreviousDate)
        val tvSuggestNewDate: TextView = view.findViewById(R.id.tvSuggestNewDate)

        val calendar = Calendar.getInstance()
        tvPreviousDate.text = "$selectedDate $selectedTime"

        btnDatePicker.text = Utils.getDateFormat().format(calendar.time)
        btnTimePicker.text = "12:00"
        val datee =
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                btnDatePicker.text = Utils.getDateFormat().format(calendar.time)
            }

        // Date Picker
        btnDatePicker.setOnClickListener {
            DatePickerDialog(
                context,
                datee,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
            ).show()
        }

        val time =
            TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
                val hour = hourOfDay.toString().padStart(2, '0')
                val minutePadded = minute.toString().padStart(2, '0')
                btnTimePicker.text = "$hour:$minutePadded"
            }

        // Time Picker
        btnTimePicker.setOnClickListener {
            TimePickerDialog(context, time, 12, 0, true).show()
        }

        // Cancel Button
        btnCancel.setOnClickListener {
            dismiss()
        }

        // Done Button
        btnDone.setOnClickListener {
            onDateSelected(btnDatePicker.text.toString(), btnTimePicker.text.toString())
            dismiss()
        }

        return AlertDialog.Builder(context)
            .setView(view)
            .create()
    }
}

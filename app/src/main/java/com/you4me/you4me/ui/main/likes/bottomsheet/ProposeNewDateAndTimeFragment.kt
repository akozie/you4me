package com.you4me.you4me.ui.main.likes.bottomsheet

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentProposeNewDateAndTimeBinding
import java.text.SimpleDateFormat
import java.util.*


class ProposeNewDateAndTimeFragment : BottomSheetDialogFragment()
{
    private lateinit var binding: FragmentProposeNewDateAndTimeBinding
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var etMessage: EditText
    private lateinit var btnCancel: Button
    private lateinit var btnSendProposal: Button
    private lateinit var tvOriginalDate: TextView

    override fun onStart() {
        super.onStart()

        val dialog = dialog ?: return
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            val layoutParams = it.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.85).toInt()
            it.layoutParams = layoutParams

            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProposeNewDateAndTimeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etDate = binding.etDate
        etTime = binding.etTime
        etMessage = binding.etMessage
        btnCancel = binding.btnCancel
        btnSendProposal = binding.btnSendProposal
        tvOriginalDate = binding.tvOriginalDate

        // Set original date from intent
        val originalDate = requireActivity().intent.getStringExtra("originalDate") ?: "June-15-2025"
        tvOriginalDate.text = "Original: $originalDate"

        etDate.setOnClickListener {
            showDatePicker()
        }

        etTime.setOnClickListener {
            showTimePicker()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnSendProposal.setOnClickListener {
            val newDate = etDate.text.toString()
            val newTime = etTime.text.toString()
            val message = etMessage.text.toString()

            // Here you can handle proposal submission (e.g., send to backend or Firebase)
            Toast.makeText(requireContext(), "Proposal Sent\n$newDate $newTime", Toast.LENGTH_SHORT).show()
            dismiss()
        }


    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val sdf = SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault())
            calendar.set(year, month, dayOfMonth)
            etDate.setText(sdf.format(calendar.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            etTime.setText(timeFormat.format(calendar.time))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
    }
}

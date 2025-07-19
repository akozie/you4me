package com.you4me.you4me.ui.main.likes.bottomsheet

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentDecliningInterestBinding
import com.you4me.you4me.databinding.FragmentReportAbuseBottomSheetBinding


class DecliningInterestFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentDecliningInterestBinding
    private var selectedReason: TextView? = null
    private val maxChar = 145


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDecliningInterestBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupSelectableReasons()
        setupCharacterCounter()
        setupSubmitButton()
    }

    private fun setupSelectableReasons() {
        val reasonFields = listOf(
            binding.etReason1,
            binding.etReason2,
            binding.etOtherReason
        )

        reasonFields.forEach { reason ->
            reason.setOnClickListener {
                // Deselect all first
                reasonFields.forEach { resetReasonStyle(it) }

                // Select current
                selectedReason = reason
                selectReasonStyle(reason)
            }
        }
    }

    private fun resetReasonStyle(editText: TextView) {
        editText.setBackgroundResource(R.drawable.bg_input)
        editText.setTypeface(null, Typeface.NORMAL)
    }

    private fun selectReasonStyle(editText: TextView) {
        editText.setBackgroundResource(R.drawable.bg_input_red)
        editText.setTypeface(null, Typeface.BOLD)
    }

    private fun setupCharacterCounter() {
        binding.etAdditionalInfo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val count = s?.length ?: 0
                binding.tvCharCount.text = "$count/$maxChar"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupSubmitButton() {
        binding.btnReport.setOnClickListener {
            val reason = selectedReason?.hint?.toString()?.trim()
            val extraInfo = binding.etAdditionalInfo.text.toString().trim()

            if (reason.isNullOrEmpty() && extraInfo.isEmpty()) {
                Toast.makeText(requireContext(), "Please select or enter a reason", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: Implement real report logic (e.g. network call)
            Toast.makeText(requireContext(), "User reported for: $reason", Toast.LENGTH_LONG).show()
        }
    }
}

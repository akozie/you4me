package com.you4me.you4me.ui.main.finddates.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentNotInterestedBinding


class NotInterestedFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentNotInterestedBinding
    private lateinit var feedbackLayouts: List<LinearLayout>
    private lateinit var feedbackTextViews: List<TextView>
    private var feedbackCompliment: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNotInterestedBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        feedbackLayouts = listOf(
            binding.reason1,
            binding.reason2,
            binding.reason3,
            binding.reason4,
            binding.reason5,
            binding.reason6,
        )

        feedbackTextViews = listOf(
            binding.reasonText1,
            binding.reasonText2,
            binding.reasonText3,
            binding.reasonText4,
            binding.reasonText5,
            binding.reasonText6
        )


        feedbackLayouts.forEachIndexed { index, layout ->
            layout.setOnClickListener {
                // Clear all selections
                feedbackLayouts.forEach { it.isSelected = false }

                // Set selected
                layout.isSelected = true

                // Get the selected compliment text
                val selectedText = feedbackTextViews[index].text.toString()
                feedbackCompliment = selectedText

                // Show or hide EditText depending on the selected reason
                if (index == 5) { // index 5 means reason6
                    binding.reason?.visibility = View.VISIBLE
                } else {
                    binding.reason?.visibility = View.GONE
                }

                Log.d("SELECTED_F", "$feedbackCompliment")
            }

        }


        binding.submitFeedbackBtn.setOnClickListener {
            val finalFeedback = if (binding.reason?.visibility == View.VISIBLE) {
                binding.otherFeedback.text.toString()
            } else {
                feedbackCompliment
            }

            if (finalFeedback.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Please select a compliment", Toast.LENGTH_SHORT).show()
            } else {
                sendCompliment(finalFeedback)
            }
        }

    }


    private fun sendCompliment(compliment: String) {
        // Send to API or show message
        Toast.makeText(context, "Compliment sent: $compliment", Toast.LENGTH_SHORT).show()
    }
}
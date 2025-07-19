package com.you4me.you4me.ui.main.finddates

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
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
                Log.d("SELECTED_F", "$feedbackCompliment")
            }
        }

        binding.submitFeedbackBtn.setOnClickListener {
            feedbackCompliment?.let {
                sendCompliment(it)
            } ?: Toast.makeText(requireContext(), "Please select a compliment", Toast.LENGTH_SHORT).show()
        }
    }


    private fun sendCompliment(compliment: String) {
        // Send to API or show message
        Toast.makeText(context, "Compliment sent: $compliment", Toast.LENGTH_SHORT).show()
    }
}
package com.you4me.you4me.ui.main.finddates.bottomsheet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentFilterBottomSheetBinding
import com.you4me.you4me.models.useroptions.AgeGroup


class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentFilterBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFilterBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ageList = listOf(
            AgeGroup("1", "18 - 24 years"),
            AgeGroup("2", "25 - 34 years"),
            AgeGroup("3", "35 - 44 years"),
            AgeGroup("4", "45 and above")
        )
        val distanceList = listOf(
            AgeGroup("1", "Within 10 miles"),
            AgeGroup("2", "Within 20 miles"),
            AgeGroup("3", "Within 30 miles"),
            AgeGroup("4", "Above 30 miles")
        )

        binding.saveBtn.setOnClickListener {
            dismiss()
        }

        binding.reset.setOnClickListener {
            binding.ageRange.text = "18 - 24 years"
            binding.distanceRange.text = "Within 10 miles"
            binding.switchCompat.isChecked = false
            dismiss()
        }

        binding.distanceRangeSlider.addOnChangeListener { _, value, _ ->
            val index = value.toInt()
            val selectedRange = distanceList.getOrNull(index)
            binding.distanceRange.text = selectedRange?.value
        }
        binding.ageRangeSlider.addOnChangeListener { _, value, _ ->
            val index = value.toInt()
            val selectedRange = ageList.getOrNull(index)
            binding.ageRange.text = selectedRange?.value
        }
    }
}
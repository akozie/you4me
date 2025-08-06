package com.you4me.you4me.ui.profile.bottomsheet

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.Slider
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentAgePreferenceBottomSheetBinding
import com.you4me.you4me.databinding.FragmentCountryBottomSheetBinding
import com.you4me.you4me.models.ValueLabelResponse
import com.you4me.you4me.models.useroptions.AgeGroup


class AgePreferenceBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentAgePreferenceBottomSheetBinding
    private lateinit var radioGroupCustom: Slider
    private var  receivedList: List<AgeGroup>? = null
    private var selectedValue: String? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAgePreferenceBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        radioGroupCustom = binding.ageRangeSlider

        binding.saveBtn.setOnClickListener {
            val age = binding.ageRange.text.toString().trim()
            val result = Bundle().apply {
                putString("selected_value", age)
                putString("sheet_id", "AGE")  // Unique tag for the sheet
            }
            setFragmentResult("bottom_sheet_result", result)
            dismiss()
        }
        binding.closeBtn.setOnClickListener {
            dismiss()
        }

         receivedList = arguments?.let {
            AgePreferenceBottomSheetFragmentArgs.fromBundle(it).AGEGROUP.toList()
        }
        Log.d("CHECKING==", "$receivedList")

        binding.ageRangeSlider.addOnChangeListener { _, value, _ ->
            val index = value.toInt()
            val selectedRange = receivedList?.getOrNull(index)
            binding.ageRange.text = selectedRange?.label
        }
    }
}
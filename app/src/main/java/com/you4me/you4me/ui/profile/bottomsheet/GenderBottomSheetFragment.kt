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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentGenderBottomSheetBinding
import com.you4me.you4me.databinding.FragmentSexualityBottomSheetBinding
import com.you4me.you4me.models.ValueLabelResponse
import com.you4me.you4me.models.useroptions.Gender


class GenderBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentGenderBottomSheetBinding
    private lateinit var radioGroupCustom: LinearLayout
    private var receivedList: List<Gender>? = null

    private var selectedValue: String? = null
    private var selectedLabel: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGenderBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        radioGroupCustom = binding.radioGroupCustom


        binding.saveBtn.setOnClickListener {
            val result = Bundle().apply {
                putString("selected_value", selectedLabel)
                putString("sheet_id", "GENDER")  // Unique tag for the sheet
            }
            setFragmentResult("bottom_sheet_result", result)
            dismiss()
        }
        binding.cancelIcon.setOnClickListener {
            dismiss()
        }
         receivedList = arguments?.let {
            GenderBottomSheetFragmentArgs.fromBundle(it).GENDER.toList()
        }
        Log.d("CHECKING==", "$receivedList")
        setupCustomRadioGroup(radioGroupCustom, receivedList)

    }


    private fun setupCustomRadioGroup(container: LinearLayout, options: List<Gender>?) {
        container.removeAllViews()

        if (options != null) {
            for ((index, item) in options.withIndex()) {
                val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_radio_button, container, false)

                val label = view.findViewById<TextView>(R.id.textOption)
                val icon = view.findViewById<ImageView>(R.id.radioIcon)

                label.text = item.label

                view.setOnClickListener {
                    selectedValue = item.value
                    selectedLabel = item.label
                    Log.d("GETT", selectedValue!!)
                    updateRadioIcons(container, index)
                }

                container.addView(view)
            }
        }
    }

    private fun updateRadioIcons(container: LinearLayout, selectedIndex: Int) {
        for (i in 0 until container.childCount) {
            val item = container.getChildAt(i)
            val icon = item.findViewById<ImageView>(R.id.radioIcon)

            icon.setImageResource(
                if (i == selectedIndex) R.drawable.ic_radio_checked
                else R.drawable.ic_radio_unchecked
            )
        }
    }
}
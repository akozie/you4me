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
import com.you4me.you4me.databinding.FragmentBioBottomSheetBinding
import com.you4me.you4me.databinding.FragmentSexualityBottomSheetBinding
import com.you4me.you4me.models.ValueLabelResponse


class BioBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBioBottomSheetBinding



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBioBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        binding.saveBtn.setOnClickListener {
            val bio = binding.bioTextTv.text.toString().trim()
            val result = Bundle().apply {
                putString("selected_value", bio)
                putString("sheet_id", "BIO")  // Unique tag for the sheet
            }
            setFragmentResult("bottom_sheet_result", result)
            dismiss()
        }
        binding.cancelIcon.setOnClickListener {
            dismiss()
        }
    }

}
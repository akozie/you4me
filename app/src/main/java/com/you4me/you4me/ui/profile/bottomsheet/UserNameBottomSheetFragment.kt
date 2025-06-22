package com.you4me.you4me.ui.profile.bottomsheet

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentBioBottomSheetBinding
import com.you4me.you4me.databinding.FragmentUserNameBottomSheetBinding
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.SharedPrefHelper.Companion.USERNAME


class UserNameBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentUserNameBottomSheetBinding
    private lateinit var sharedPrefHelper: SharedPrefHelper
    private var receivedList: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUserNameBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefHelper = SharedPrefHelper(requireContext())
        isCancelable = false
        receivedList = arguments?.let {
            UserNameBottomSheetFragmentArgs.fromBundle(it).USERNAME
        }

        binding.saveBtn.setOnClickListener {
            val name = binding.bioTextTv.text.toString().trim()
            val result = Bundle().apply {
                putString("selected_value", name)
                putString("sheet_id", "NAME")  // Unique tag for the sheet
            }
            setFragmentResult("bottom_sheet_result", result)
            dismiss()
        }

        binding.cancelIcon.setOnClickListener {
            dismiss()
        }

        Log.d("THE_STRING", receivedList.toString())
        binding.bioTextTv.setText(receivedList.toString())
    }
}
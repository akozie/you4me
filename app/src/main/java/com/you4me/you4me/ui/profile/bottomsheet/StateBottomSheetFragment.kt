package com.you4me.you4me.ui.profile.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentStateBottomSheetBinding
import com.you4me.you4me.models.ValueLabelResponse
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.closeSoftKeyboard
import com.you4me.you4me.utils.hideKeyboard


class StateBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentStateBottomSheetBinding
    private lateinit var radioGroupCustom: LinearLayout
    private var receivedList: List<ValueLabelResponse>? = null
    private var selectedValue: String? = null
    private var selectedLabel: String? = null
    private lateinit var sharedPrefHelper: SharedPrefHelper

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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStateBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        sharedPrefHelper = SharedPrefHelper(requireContext())

        radioGroupCustom = binding.radioGroupCustom


        binding.saveBtn.setOnClickListener {
            val result = Bundle().apply {
                putString("selected_value", selectedLabel)
                putString("sheet_id", "STATE")  // Unique tag for the sheet
            }
            setFragmentResult("bottom_sheet_result", result)
            dismiss()
        }
        binding.cancelIcon.setOnClickListener {
            dismiss()
        }
        receivedList = arguments?.let {
            StateBottomSheetFragmentArgs.fromBundle(it).STATE.toList()
        }
        setupCustomRadioGroup(radioGroupCustom, receivedList)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = receivedList?.filter {
                    it.label.contains(newText.orEmpty(), ignoreCase = true)
                }
                setupCustomRadioGroup(radioGroupCustom, filtered)
                return true
            }
        })
    }

    private fun setupCustomRadioGroup(container: LinearLayout, options: List<ValueLabelResponse>?) {
        container.removeAllViews()
        Log.d("FIND_SATES==", "$options")

        if (options != null) {
            for ((index, item) in options.withIndex()) {
                val view = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_radio_button, container, false)

                val label = view.findViewById<TextView>(R.id.textOption)
                val icon = view.findViewById<ImageView>(R.id.radioIcon)

                label.text = item.label

                view.setOnClickListener {
                    selectedValue = item.value
                    selectedLabel = item.label
                    updateRadioIcons(container, index)
                    hideKeyboard()
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
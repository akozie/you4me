package com.you4me.you4me.ui.main.finddates

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentAllCompletedDatesBinding
import com.you4me.you4me.databinding.FragmentSuggestedBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainViewModel


class SuggestedFragment :
    BaseFragment<MainViewModel, FragmentSuggestedBinding, MainRepository>("SUGGESTED_DATES") {

    private lateinit var complimentLayouts: List<LinearLayout>
    private lateinit var complimentTextViews: List<TextView>
    private var selectedCompliment: String? = null

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSuggestedBinding {
        return FragmentSuggestedBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        complimentLayouts = listOf(
            binding.compliment1,
            binding.compliment2,
            binding.compliment3,
            binding.compliment4,
            binding.compliment5,
            binding.compliment6,
        )

        complimentTextViews = listOf(
            binding.complimentText1,
            binding.complimentText2,
            binding.complimentText3,
            binding.complimentText4,
            binding.complimentText5,
            binding.complimentText6,
        )


        complimentLayouts.forEachIndexed { index, layout ->
            layout.setOnClickListener {
                // Clear all selections
                complimentLayouts.forEach { it.isSelected = false }

                // Set selected
                layout.isSelected = true

                // Get the selected compliment text
                val selectedText = complimentTextViews[index].text.toString()
                selectedCompliment = selectedText
                binding.sendComplimentsBtn.isEnabled = true
            }
        }

        binding.sendComplimentsBtn.setOnClickListener {
            selectedCompliment?.let {
                sendCompliment(it)
            } ?: showToast("Please select a compliment")
            Log.d("SELECTED_", "$selectedCompliment")
        }
    }


    private fun sendCompliment(compliment: String) {
        // Send to API or show message
        findNavController().popBackStack()
        Toast.makeText(context, "Compliment sent: $compliment", Toast.LENGTH_SHORT).show()
    }
    }
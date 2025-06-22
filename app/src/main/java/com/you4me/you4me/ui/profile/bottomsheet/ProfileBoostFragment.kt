package com.you4me.you4me.ui.profile.bottomsheet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentProfileBoostBinding


class ProfileBoostFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentProfileBoostBinding
    private lateinit var optionOne: MaterialCardView
    private lateinit var optionTwo: MaterialCardView
    private lateinit var optionThree: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBoostBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        optionOne = binding.optionOneBoost
        optionTwo = binding.optionTwoBoosts
        optionThree = binding.optionThreeBoosts

        val allOptions = listOf(optionOne, optionTwo, optionThree)

        allOptions.forEach { card ->
            card.setOnClickListener {
                allOptions.forEach { it.isSelected = false }
                card.isSelected = true
            }
        }
    }
}
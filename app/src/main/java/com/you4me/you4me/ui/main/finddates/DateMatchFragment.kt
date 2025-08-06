package com.you4me.you4me.ui.main.finddates

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentDateMatchBinding


class DateMatchFragment : Fragment() {

    private lateinit var binding: FragmentDateMatchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDateMatchBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireContext())
            .load(R.drawable.onboarding_img1)
            .placeholder(R.drawable.onboarding_img3)
            .error(R.drawable.onboarding_img3)
            .circleCrop()
            .into(binding.userImage)

        Glide.with(requireContext())
            .load(R.drawable.onboarding_img3)
            .placeholder(R.drawable.onboarding_img3)
            .error(R.drawable.onboarding_img3)
            .circleCrop()
            .into(binding.matchImage)

        binding.okayButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

}
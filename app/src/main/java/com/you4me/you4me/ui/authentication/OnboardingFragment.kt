package com.you4me.you4me.ui.authentication

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView.OnScrollListener
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.you4me.you4me.R
import com.you4me.you4me.adapter.OnboardingRecyclerAdapter
import com.you4me.you4me.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {
    private lateinit var binding: FragmentOnboardingBinding
    private var curPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingBinding.inflate(layoutInflater)
        if (isOnBoardingDone()) findNavController().navigate(R.id.action_onboardingFragment_to_loginFragment)
        else setup()
        return binding.root
    }

    private fun isOnBoardingDone(): Boolean {
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sharedPref?.getBoolean("Finished", false) ?: false
    }

    private fun setup() {
        val adapter = OnboardingRecyclerAdapter(this)
        binding.onboardingRecycler.adapter = adapter

        binding.nextBtn.setOnClickListener {
            curPosition++
            binding.onboardingRecycler.smoothScrollToPosition(curPosition)
            setPosition(curPosition)
        }

        binding.onboardingRecycler.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dx == 0) return
                    val manager = binding.onboardingRecycler.layoutManager as LinearLayoutManager

                    curPosition = if (dx > 0) {
                        manager.findLastVisibleItemPosition()
                    } else {
                        manager.findFirstVisibleItemPosition()
                    }

                    binding.onboardingRecycler.smoothScrollToPosition(curPosition)
                    setPosition(curPosition)
                }
            }
        )

        binding.loginBtn.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment_to_loginFragment)
            onBoardingFinished()
        }
        binding.registerBtn.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment_to_registrationFragment)
            onBoardingFinished()
        }
    }

    private fun setPosition(pos: Int) {
        val pink =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_pink))
        val grey =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey_circle))

        when (pos) {
            0 -> {
                ImageViewCompat.setImageTintList(binding.circle1, pink)
                ImageViewCompat.setImageTintList(binding.circle2, grey)
                ImageViewCompat.setImageTintList(binding.circle3, grey)
                binding.endLyt.visibility = View.GONE
                binding.nextBtn.visibility = View.VISIBLE
            }

            1 -> {
                ImageViewCompat.setImageTintList(binding.circle1, grey)
                ImageViewCompat.setImageTintList(binding.circle2, pink)
                ImageViewCompat.setImageTintList(binding.circle3, grey)
                binding.endLyt.visibility = View.GONE
                binding.nextBtn.visibility = View.VISIBLE
            }

            2 -> {
                ImageViewCompat.setImageTintList(binding.circle1, grey)
                ImageViewCompat.setImageTintList(binding.circle2, grey)
                ImageViewCompat.setImageTintList(binding.circle3, pink)
                binding.endLyt.visibility = View.VISIBLE
                binding.nextBtn.visibility = View.GONE
            }
        }
    }

    private fun onBoardingFinished() {
        val sharedPref = activity?.getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()
        editor?.putBoolean("Finished", true)
        editor?.apply()
    }
}
package com.you4me.you4me.ui.main.finddates.bottomsheet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.you4me.you4me.R
import com.you4me.you4me.adapter.ComplimentsPagerAdapter
import com.you4me.you4me.adapter.FindDatesPagerAdapter
import com.you4me.you4me.databinding.FragmentComplimentsBottomSheetBinding


class ComplimentsBottomSheetFragment : Fragment() {

    private lateinit var binding: FragmentComplimentsBottomSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentComplimentsBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewPager()
    }

    private fun setUpViewPager() {
        val adapter = ComplimentsPagerAdapter(this, 2) // ✅ Pass `this` (fragment)
        binding.pager.adapter = adapter
        binding.pager.isUserInputEnabled = true // ✅ Ensure swiping is enabled

        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            tab.text =
                when (position) {
                    0 -> getString(R.string.custom)
                    1 -> getString(R.string.suggested)
                    else -> getString(R.string.custom)
                }
        }.attach()

    }

}
package com.you4me.you4me.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.you4me.you4me.R
import com.you4me.you4me.adapter.DatesInterestPagerAdapter
import com.you4me.you4me.databinding.FragmentDatesInterestBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment

class DatesInterestFragment : BaseFragment<MainViewModel, FragmentDatesInterestBinding, MainRepository>("PROPOSE_DATE_TIME") {
    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentDatesInterestBinding {
        return FragmentDatesInterestBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewPager()
    }

    private fun setUpViewPager() {
        val adapter = DatesInterestPagerAdapter(this, 2) // ✅ Pass `this` (fragment)
        binding.pager.adapter = adapter
        binding.pager.isUserInputEnabled = true // ✅ Ensure swiping is enabled

        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            tab.text =
                when (position) {
                    0 -> getString(R.string.received_requests)
                    1 -> getString(R.string.sent_requests)
                    else -> getString(R.string.received_requests)
                }
        }.attach()

        // ✅ Debugging Log
        binding.pager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    Log.d("ViewPagerDebug", "Page changed to: $position")
                    when (position) { // When the second screen is selected
                        0 -> {
                            binding.titleText.text = getString(R.string.interested_in_you)
                            binding.titleSub.text = getString(R.string.interest_in_date)
                        }
                        1 -> { // When the second screen is selected
                            binding.titleText.text = "People you are interested in"
                            binding.titleSub.text =
                                "Check out people you have sent date interest to"
                        }
                        else -> {
                            binding.titleText.text = getString(R.string.interested_in_you)
                            binding.titleSub.text = getString(R.string.interest_in_date)
                        }
                    }
                }
            },
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mixpanel?.mixpanel?.flush()
        mixpanel?.mixpanel?.optOutTracking()
    }
}

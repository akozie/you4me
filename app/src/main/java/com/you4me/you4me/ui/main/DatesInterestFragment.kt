package com.you4me.you4me.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.you4me.you4me.R
import com.you4me.you4me.adapter.DatesInterestPagerAdapter
import com.you4me.you4me.databinding.FragmentDatesInterestBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment

class DatesInterestFragment : BaseFragment<MainViewModel, FragmentDatesInterestBinding, MainRepository>() {
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout

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
        viewPager2 = binding.pager
        tabLayout = binding.tabs
        val adapter = DatesInterestPagerAdapter(childFragmentManager, lifecycle, 2)
        viewPager2.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.received_requests)
                1 -> tab.text = getString(R.string.sent_requests)
            }
        }.attach()
    }
}

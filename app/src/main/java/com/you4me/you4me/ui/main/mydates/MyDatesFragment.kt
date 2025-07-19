package com.you4me.you4me.ui.main.mydates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.you4me.you4me.R
import com.you4me.you4me.adapter.MyDatesPagerAdapter
import com.you4me.you4me.databinding.FragmentMyDatesBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainViewModel


class MyDatesFragment :
    BaseFragment<MainViewModel, FragmentMyDatesBinding, MainRepository>("MY_DATES_SCREEN") {



    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentMyDatesBinding {
        return FragmentMyDatesBinding.inflate(layoutInflater)
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
        val adapter = MyDatesPagerAdapter(this, 3) // ✅ Pass `this` (fragment)
        binding.pager.adapter = adapter
        binding.pager.isUserInputEnabled = true // ✅ Ensure swiping is enabled

        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            tab.text =
                when (position) {
                    0 -> getString(R.string.approved)
                    1 -> getString(R.string.upcoming)
                    2 -> getString(R.string.completed)
                    else -> getString(R.string.approved)
                }
        }.attach()

    }

    override fun onDestroy() {
        super.onDestroy()
        mixpanel?.mixpanel?.flush()
        mixpanel?.mixpanel?.optOutTracking()
    }
}
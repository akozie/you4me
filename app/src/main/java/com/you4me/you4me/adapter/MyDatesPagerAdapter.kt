package com.you4me.you4me.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.you4me.you4me.databinding.ApprovedDateItemBinding
import com.you4me.you4me.ui.main.*
import com.you4me.you4me.ui.main.finddates.DiscoverFragment
import com.you4me.you4me.ui.main.finddates.NearbyFragment
import com.you4me.you4me.ui.main.mydates.ApprovedMyDatesFragment
import com.you4me.you4me.ui.main.mydates.CompletedMyDatesFragment
import com.you4me.you4me.ui.main.mydates.UpcomingMyDatesFragment

class MyDatesPagerAdapter(fragment: Fragment, private val NUM_PAGES: Int) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount() = NUM_PAGES

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ApprovedMyDatesFragment()
            1 -> UpcomingMyDatesFragment()
            2 -> CompletedMyDatesFragment()
            else -> {
                ApprovedMyDatesFragment()
            }
        }
    }
}

package com.you4me.you4me.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.you4me.you4me.ui.main.LikesFragment
import com.you4me.you4me.ui.main.SentRequestsFragment

class DatesInterestPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, private val NUM_PAGES: Int) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return NUM_PAGES
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> SentRequestsFragment()
            else -> LikesFragment()
        }
    }
}

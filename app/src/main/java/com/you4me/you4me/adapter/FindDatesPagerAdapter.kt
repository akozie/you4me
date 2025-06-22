package com.you4me.you4me.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.you4me.you4me.ui.main.LikesFragment
import com.you4me.you4me.ui.main.SentRequestsFragment
import com.you4me.you4me.ui.main.finddates.DiscoverFragment
import com.you4me.you4me.ui.main.finddates.NearbyFragment

class FindDatesPagerAdapter(fragment: Fragment, private val NUM_PAGES: Int) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount() = NUM_PAGES

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DiscoverFragment()
            1 -> NearbyFragment()
            else -> DiscoverFragment()
        }
    }
}

package com.you4me.you4me.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.you4me.you4me.ui.main.CompletedDatesFragment
import com.you4me.you4me.ui.main.LikesFragment
import com.you4me.you4me.ui.main.SentRequestsFragment
import com.you4me.you4me.ui.main.UpcomingDatesFragment

class HomePagerAdapter(fragment: Fragment, private val NUM_PAGES: Int) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount() = NUM_PAGES

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UpcomingDatesFragment()
            1 -> CompletedDatesFragment()
            else -> UpcomingDatesFragment()
        }
    }
}

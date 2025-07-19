package com.you4me.you4me.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.you4me.you4me.ui.main.finddates.CustomFragment
import com.you4me.you4me.ui.main.finddates.SuggestedFragment

class ComplimentsPagerAdapter(fragment: Fragment, private val NUM_PAGES: Int) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount() = NUM_PAGES

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SuggestedFragment()
            1 -> CustomFragment()
            else -> SuggestedFragment()
        }
    }
}

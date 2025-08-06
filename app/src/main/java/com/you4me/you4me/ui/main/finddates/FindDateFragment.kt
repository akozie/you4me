package com.you4me.you4me.ui.main.finddates

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.you4me.you4me.R
import com.you4me.you4me.adapter.DatesInterestPagerAdapter
import com.you4me.you4me.adapter.FindDatesPagerAdapter
import com.you4me.you4me.databinding.FragmentFindDateBinding
import com.you4me.you4me.model.User
import com.you4me.you4me.models.*
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainViewModel
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.Utils.getCategoryFromString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FindDateFragment :
    BaseFragment<MainViewModel, FragmentFindDateBinding, MainRepository>("FIND_DATE") {

    private val tabTitles = listOf("Discover", "Nearby")

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentFindDateBinding {
        return FragmentFindDateBinding.inflate(layoutInflater)
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
        val adapter = FindDatesPagerAdapter(this, 2) // ✅ Pass `this` (fragment)
        binding.pager.adapter = adapter
//        binding.pager.isUserInputEnabled = true // ✅ Ensure swiping is enabled

        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            val customTabView = LayoutInflater.from(context)
                .inflate(R.layout.custom_tab, null, false) as TextView


            customTabView.text = tabTitles[position]
            tab.customView = customTabView

        }.attach()

        // Optional: set selected state manually on startup
        binding.tabs.getTabAt(0)?.customView?.isSelected = true

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tab.customView?.isSelected = true
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.customView?.isSelected = false
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mixpanel?.mixpanel?.flush()
        mixpanel?.mixpanel?.optOutTracking()
    }






}

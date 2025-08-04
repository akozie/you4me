package com.you4me.you4me.ui.main.finddates.bottomsheet

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.you4me.you4me.R
import com.you4me.you4me.adapter.ComplimentsPagerAdapter
import com.you4me.you4me.adapter.FindDatesPagerAdapter
import com.you4me.you4me.databinding.FragmentComplimentsBottomSheetBinding


class ComplimentsBottomSheetFragment : BottomSheetDialogFragment() {

    private val tabTitles = listOf("Suggested", "Custom")

    private lateinit var binding: FragmentComplimentsBottomSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentComplimentsBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true

                // Set height to 75% of the screen
                val screenHeight = resources.displayMetrics.heightPixels
                val screenWidth = resources.displayMetrics.widthPixels
                val desiredHeight = (screenHeight * 0.75).toInt()
                it.layoutParams.height = desiredHeight
                it.layoutParams.width = screenWidth
                it.requestLayout()
            }
        }

        return dialog
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewPager()
    }

    private fun setUpViewPager() {
        val adapter = ComplimentsPagerAdapter(this, 2)
        binding.pager.adapter = adapter
        binding.pager.isUserInputEnabled = true

        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            val customTabView = LayoutInflater.from(context)
                .inflate(R.layout.custom_tab, null, false) as TextView


            customTabView.text = tabTitles[position]
            tab.customView = customTabView

        }.attach()
    }

}
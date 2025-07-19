package com.you4me.you4me.ui.profile.bottomsheet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentProfileBoostBinding


class ProfileBoostFragment : BottomSheetDialogFragment() {

    private lateinit var boostOptions: List<View>
    private lateinit var binding: FragmentProfileBoostBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBoostBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        boostOptions = listOf(
            view.findViewById(R.id.boostOption1),
            view.findViewById(R.id.boostOption2),
            view.findViewById(R.id.boostOption3)
        )

        setupBoostOption(
            boostOptions[0],
            "2 Boosts", "24hrs", "$11.59 each", "", "Save 11%"
        )

        setupBoostOption(
            boostOptions[1],
            "3 Boosts", "24hrs", "$10.59 each", "For 3 boosts, then $31.0", "Save 5%"
        )

        setupBoostOption(
            boostOptions[2],
            "1 Boost", "24hrs", "$15.59 each", "", "Save 2%"
        )

        boostOptions.forEach { option ->
            option.setOnClickListener {
                selectOption(option)
            }
        }
    }

    private fun setupBoostOption(
        view: View,
        title: String,
        duration: String,
        price: String,
        subText: String,
        discount: String
    ) {
        view.findViewById<TextView>(R.id.tvBoostTitle).text = title
        view.findViewById<TextView>(R.id.tvBoostDuration).text = duration
        view.findViewById<TextView>(R.id.tvBoostPrice).text = price
        view.findViewById<TextView>(R.id.tvBoostSubtext).apply {
            text = subText
            visibility = if (subText.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun selectOption(selectedView: View) {
        boostOptions.forEach { option ->
            option.findViewById<LinearLayout>(R.id.boostContainer).background =
                ContextCompat.getDrawable(
                    requireContext(),
                    if (option == selectedView) R.drawable.bg_boost_selected
                    else R.drawable.bg_boost_default
                )
        }
    }
}

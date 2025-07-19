package com.you4me.you4me.ui.main.mydates

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentApprovedMyDatesBinding
import com.you4me.you4me.databinding.FragmentGoOnDateBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainViewModel


class ApprovedMyDatesFragment :
    BaseFragment<MainViewModel, FragmentApprovedMyDatesBinding, MainRepository>("APPROVED_MY_DATE_SCREEN") {

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentApprovedMyDatesBinding.inflate(layoutInflater)

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.planADateBtn.setOnClickListener {
            findNavController().navigate(R.id.goOnDateFragment)
        }
    }

}
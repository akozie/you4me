package com.you4me.you4me.ui.main.finddates

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentFindDateBinding
import com.you4me.you4me.databinding.FragmentNearbyBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainViewModel


class NearbyFragment :
    BaseFragment<MainViewModel, FragmentNearbyBinding, MainRepository>("FIND_DATE") {

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentNearbyBinding {
        return FragmentNearbyBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
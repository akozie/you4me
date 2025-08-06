package com.you4me.you4me.ui.main.finddates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.you4me.you4me.databinding.FragmentCustomBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainViewModel


class CustomFragment :
    BaseFragment<MainViewModel, FragmentCustomBinding, MainRepository>("CUSTOM_COMPLIMENT_SCREEN") {


    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCustomBinding {
        return FragmentCustomBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sendComplimentsBtn.setOnClickListener {
            if (binding.bioTextTv.text.toString().trim().isEmpty()) {
                showToast("Please write your own compliment")
            } else {
                findNavController().popBackStack()
            }
        }
    }
}
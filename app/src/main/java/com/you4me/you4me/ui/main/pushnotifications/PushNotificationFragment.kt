package com.you4me.you4me.ui.main.pushnotifications

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentPushNotificationBinding
import com.you4me.you4me.databinding.FragmentSettingsBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainViewModel


class PushNotificationFragment :
    BaseFragment<MainViewModel, FragmentPushNotificationBinding, MainRepository>("PUSH_NOTIFICATION"){

    private lateinit var switchEnableAll: Switch
    private lateinit var switchMuteAll: Switch
    private lateinit var switchCompliments: Switch
    private lateinit var switchInterests: Switch
    private lateinit var switchMessages: Switch
    private lateinit var switchDates: Switch


    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentPushNotificationBinding {
        return FragmentPushNotificationBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchEnableAll = binding.switchEnableAll
        switchMuteAll = binding.switchMuteAll
        switchCompliments = binding.switchCompliments
        switchInterests = binding.switchInterests
        switchMessages = binding.switchMessages
        switchDates = binding.switchDates

        // Set initial state based on your image
        switchEnableAll.isChecked = true
        switchMuteAll.isChecked = false
        switchCompliments.isChecked = true
        switchInterests.isChecked = false
        switchMessages.isChecked = true
        switchDates.isChecked = false

        switchEnableAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchMuteAll.isChecked = false
                enableAllToggles(true)
            } else {
                enableAllToggles(false)
            }
        }

        switchMuteAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchEnableAll.isChecked = false
                enableAllToggles(false)
            }
        }
    }
    private fun enableAllToggles(enable: Boolean) {
        switchCompliments.isChecked = enable
        switchInterests.isChecked = enable
        switchMessages.isChecked = enable
        switchDates.isChecked = enable
    }


}

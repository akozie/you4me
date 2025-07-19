package com.you4me.you4me.ui.main.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentHomeBinding
import com.you4me.you4me.databinding.FragmentSettingsBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainViewModel


class SettingsFragment :
    BaseFragment<MainViewModel, FragmentSettingsBinding, MainRepository>("SETTINGS"){

    private lateinit var switchLastActive: Switch
    private lateinit var emailEdit: TextView
    private lateinit var passwordEdit: TextView
    private lateinit var btnLogout: Button
    private lateinit var deleteAccount: TextView

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchLastActive = binding.switchLastActive
        emailEdit = binding.emailEdit
        passwordEdit = binding.passwordEdit
        btnLogout = binding.btnLogout
        deleteAccount = binding.deleteAccount

        // Restore switch state
        val isLastActiveVisible = sharedPrefHelper.getBoolean("last_active_status")
        switchLastActive.isChecked = isLastActiveVisible

        switchLastActive.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefHelper.saveBoolean("last_active_status", isChecked)

            Toast.makeText(requireContext(), if (isChecked) "Status enabled" else "Status disabled", Toast.LENGTH_SHORT).show()
        }

        emailEdit.setOnClickListener {
            // Launch email update screen
            Toast.makeText(requireContext(), "Change Email Clicked", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, ChangeEmailActivity::class.java))
        }

        passwordEdit.setOnClickListener {
            Toast.makeText(requireContext(), "Change Password Clicked", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        btnLogout.setOnClickListener {
            // Clear session or token
            Toast.makeText(requireContext(), "Logging out...", Toast.LENGTH_SHORT).show()
            // Perform logout and redirect to login screen
            // startActivity(Intent(this, LoginActivity::class.java))
            // finish()
        }

        deleteAccount.setOnClickListener {
            Toast.makeText(requireContext(), "Delete account clicked", Toast.LENGTH_SHORT).show()
            // Confirm & delete account logic
        }

        // Optional: handle XML `onClick` attributes
        setupOnClickMethods()

    }
    private fun setupOnClickMethods() {
        binding.privacyPolicy.setOnClickListener {
            Toast.makeText(requireContext(), "Privacy Policy", Toast.LENGTH_SHORT).show()
        }

        binding.termsService.setOnClickListener {
            Toast.makeText(requireContext(), "Terms of Service", Toast.LENGTH_SHORT).show()
        }
    }

    // For XML-defined onClick handlers (alternative approach)
    fun onVerificationClick(view: android.view.View) {
        Toast.makeText(requireContext(), "Verification Clicked", Toast.LENGTH_SHORT).show()
    }

    fun onPushNotificationClick(view: android.view.View) {
        Toast.makeText(requireContext(), "Push Notification Settings", Toast.LENGTH_SHORT).show()
    }

    fun onSubscriptionClick(view: android.view.View) {
        Toast.makeText(requireContext(), "Manage Subscription", Toast.LENGTH_SHORT).show()
    }

    fun onLanguageClick(view: android.view.View) {
        Toast.makeText(requireContext(), "Change Language", Toast.LENGTH_SHORT).show()
    }

    fun onPrivacyClick(view: android.view.View) {
        Toast.makeText(requireContext(), "Privacy Policy", Toast.LENGTH_SHORT).show()
    }

    fun onTermsClick(view: android.view.View) {
        Toast.makeText(requireContext(), "Terms of Service", Toast.LENGTH_SHORT).show()
    }

    fun onDeleteAccountClick(view: android.view.View) {
        Toast.makeText(requireContext(), "Account deletion process started", Toast.LENGTH_SHORT).show()
        // Optional: Show confirmation dialog
    }
}

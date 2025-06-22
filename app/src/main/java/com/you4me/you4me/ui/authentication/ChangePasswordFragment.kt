package com.you4me.you4me.ui.authentication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentChangePasswordBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.AuthenticationRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.SharedPrefHelper.Companion.RESET_TOKEN
import com.you4me.you4me.utils.validatePassword


class ChangePasswordFragment :
    BaseFragment<AuthenticationViewModel, FragmentChangePasswordBinding, AuthenticationRepository>("VERIFICATION_SCREEN") {

    private lateinit var resetToken: String

    override fun getViewModel() = AuthenticationViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentChangePasswordBinding {
        return FragmentChangePasswordBinding.inflate(inflater, container, false)
    }

    override fun getRepository() =
        AuthenticationRepository(dataSource.buildApi(ApiCollector::class.java))


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resetToken = sharedPrefHelper.getString(RESET_TOKEN)

        binding.updatePasswordBtn.setOnClickListener {
//            findNavController().navigate(R.id.action_changePasswordFragment_to_passwordResetSuccessfulBottomSheetFragment)
            changePassword()
        }

        binding.password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Enable the button only if the EditText is not empty
            }

            override fun afterTextChanged(s: Editable?) {
                validatePassword()
            }
        })
        binding.confirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Enable the button only if the EditText is not empty
                binding.updatePasswordBtn.isEnabled = !s.isNullOrBlank()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun changePassword() {
        val password = binding.password.text.toString().trim()
        val confirmPassword = binding.confirmPassword.text.toString().trim()

        if (validatePassword(password, confirmPassword)) {
            showLoader(true)
            viewModel.resetPassword(resetToken, password)
        }
        viewModel.resetPasswordResponse.observe(viewLifecycleOwner) {
            showLoader(false)
            when (it) {
                is Resource.Success -> {
                    showToast(it.value.message)
                    findNavController().navigate(R.id.action_changePasswordFragment_to_passwordResetSuccessfulBottomSheetFragment)
                }

                is Resource.Failure -> {
                    val message =
                        if (it.isNetworkError) "Please check your internet" else it.message
                    showAlertDialog(
                        requireContext(),
                        message ?: it.errorBody ?: "Please try again",
                        "OK",
                    ) {}
                }
            }
        }
    }

    private fun validatePassword(password: String, confirmPassword: String): Boolean {
        if (password.validatePassword()) {
            if (password == confirmPassword) {
                return true
            } else {
                binding.confirmPasswordLyt.error =
                    "Confirm password and password must be the same"
            }
        } else {
            binding.passwordLyt.error = "Enter a valid password with at least 3 characters"
        }
        return false
    }

    private fun validatePassword(): Boolean {
        val password = binding.password.text.toString().trim()
        val layout = binding.passwordLyt

        val errors = mutableListOf<String>()

        if (password.length < 6) {
            errors.add("Minimum 6 characters")
        }
        if (!password.any { it.isUpperCase() }) {
            errors.add("At least one uppercase letter")
        }
        if (!password.any { it.isLowerCase() }) {
            errors.add("At least one lowercase letter")
        }
        if (!password.any { it.isDigit() }) {
            errors.add("At least one digit")
        }
        if (!password.matches(Regex(".*[!@#\$%^&*(),.?\":{}|<>\\[\\]~`_+=/\\\\'-].*"))) {
            errors.add("At least one special character")
        }

        return if (errors.isNotEmpty()) {
            layout.error = errors.joinToString("\n")
            false
        } else {
            layout.error = null
            binding.updatePasswordBtn.isEnabled = true
            true
        }
    }

    private fun showLoader(show: Boolean) {
        binding.progressCircular.visibility = if (show) View.VISIBLE else View.GONE
        binding.updatePasswordBtn.visibility = if (show) View.GONE else View.VISIBLE
        binding.updatePasswordBtn.isEnabled = !show
    }
}
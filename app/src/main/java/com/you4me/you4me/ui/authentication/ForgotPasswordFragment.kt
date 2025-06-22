package com.you4me.you4me.ui.authentication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentForgotPasswordBinding
import com.you4me.you4me.databinding.FragmentLoginBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.AuthenticationRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.SharedPrefHelper.Companion.USER_EMAIL
import com.you4me.you4me.utils.validateEmail


class ForgotPasswordFragment :
    BaseFragment<AuthenticationViewModel, FragmentForgotPasswordBinding, AuthenticationRepository>("FORGOTPASSWORD") {

    override fun getViewModel() = AuthenticationViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentForgotPasswordBinding.inflate(inflater, container, false)

    override fun getRepository() =
        AuthenticationRepository(dataSource!!.buildApi(ApiCollector::class.java))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
//        forgotPassword()

        binding.email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Enable the button only if the EditText is not empty
                binding.forgotPasswordBtn.isEnabled = !s.isNullOrBlank()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.forgotPasswordBtn.setOnClickListener {
//            findNavController().navigate(R.id.action_forgotPasswordFragment_to_verificationCodeFragment)
            forgotPassword()

        }


    }

    private fun forgotPassword(){
        val email = binding.email.text.toString().trim()
        if (email.validateEmail()){
            showLoader(true)
            viewModel.requestPasswordReset(email)
        } else {
            showToast("Please enter a valid email")
        }

        viewModel.registerRequestPasswordResetResponse.observe(viewLifecycleOwner) {
            showLoader(false)
            when (it) {
                is Resource.Success -> {
                    sharedPrefHelper.saveString(USER_EMAIL, email)
                    showToast(it.value.message)
                    findNavController().navigate(R.id.action_forgotPasswordFragment_to_verificationCodeFragment)
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

    private fun showLoader(show: Boolean) {
        binding.progressCircular.visibility = if (show) View.VISIBLE else View.GONE
        binding.forgotPasswordBtn.visibility = if (show) View.GONE else View.VISIBLE
        binding.email.isEnabled = !show
        binding.forgotPasswordBtn.isEnabled = !show
    }
}
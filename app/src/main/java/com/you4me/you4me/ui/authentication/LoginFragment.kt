package com.you4me.you4me.ui.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.you4me.you4me.AppDatabase
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentLoginBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.AuthenticationRepository
import com.you4me.you4me.repository.DbRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainActivity
import com.you4me.you4me.utils.validateEmail
import com.you4me.you4me.utils.validatePassword

class LoginFragment :
    BaseFragment<AuthenticationViewModel, FragmentLoginBinding, AuthenticationRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.clearUser()
        setupViews()
    }

    override fun getViewModel() = AuthenticationViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentLoginBinding.inflate(inflater, container, false)

    override fun getRepository() =
        AuthenticationRepository(dataSource.buildApi(ApiCollector::class.java))

    private fun setupViews() {
        viewModel.loginResponse.observe(viewLifecycleOwner) {
            showLoader(false)
            when (it) {
                is Resource.Success -> {
                    //Store data and navigate
                    showDialog("Login Successful", true)
                    viewModel.saveUser(it.value)
                    val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
                    val editor = sharedPref?.edit()
                    editor?.putString("user_id", it.value.userId)
                    editor?.apply()

                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                }

                is Resource.Failure -> {
                    val message =
                        if (it.isNetworkError) "Please check your internet" else it.message
                    showDialog(message ?: it.errorBody ?: "Please try again", true)
                }
            }
        }
        binding.loginBtn.setOnClickListener {
            val email = binding.email.text?.trim()
            val password = binding.password.text?.trim()

            if (validate(email, password)) {
                binding.emailLyt.error = null
                binding.passwordLyt.error = null
                showLoader(true)
                viewModel.login(email.toString(), password.toString())
            }
        }
        binding.signUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }
    }

    private fun showLoader(show: Boolean) {
        binding.progressCircular.visibility = if (show) View.VISIBLE else View.GONE
        binding.loginBtn.visibility = if (show) View.GONE else View.VISIBLE
        binding.email.isEnabled = !show
        binding.password.isEnabled = !show
    }
    private fun validate(email: CharSequence?, password: CharSequence?): Boolean {
        if (email.validateEmail()) {
            if (password.validatePassword()) {
                return true
            } else binding.passwordLyt.error = "Enter a valid password with at least 3 characters"
        } else binding.emailLyt.error = "Enter a valid email"
        return false
    }
}
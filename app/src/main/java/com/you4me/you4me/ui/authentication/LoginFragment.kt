package com.you4me.you4me.ui.authentication

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.you4me.you4me.databinding.FragmentLoginBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.AuthenticationRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.validateEmail
import com.you4me.you4me.utils.validatePassword

class LoginFragment :
    BaseFragment<AuthenticationViewModel, FragmentLoginBinding, AuthenticationRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        viewModel.loginResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {

                }

                is Resource.Failure -> {

                }
            }
        })
        binding.loginBtn.setOnClickListener {
            val email = binding.email.text
            val password = binding.email.text

            if (validate(email, password)) {
                binding.progressCircular.show()
                viewModel.login("a", "b")
            }
        }
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
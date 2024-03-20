package com.you4me.you4me.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentRegistrationBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.AuthenticationRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.validateEmail
import com.you4me.you4me.utils.validatePassword


class RegistrationFragment :
    BaseFragment<AuthenticationViewModel, FragmentRegistrationBinding, AuthenticationRepository>() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()

        // Google sign upp setup
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(ctx, gso)

        binding.googleSignUpBtn.setOnClickListener {
            val account = GoogleSignIn.getLastSignedInAccount(ctx)
            if (account != null) {
                //navigate to dashboard
                showToast("Already signed in")
            } else {
                val signInIntent = mGoogleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
            } catch (e: ApiException) {
                Log.w("Registration", "Google sign in failed", e)

            }
        }
    }

    override fun getViewModel() = AuthenticationViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRegistrationBinding {
        return FragmentRegistrationBinding.inflate(inflater, container, false)
    }

    override fun getRepository() =
        AuthenticationRepository(dataSource.buildApi(ApiCollector::class.java))

    private fun setupViews() {
        binding.login.setOnClickListener { findNavController().popBackStack() }
        viewModel.registerResponse.observe(viewLifecycleOwner) {
            showLoader(false)
            when (it) {
                is Resource.Success -> {
                    Toast.makeText(
                        requireActivity(),
                        getText(R.string.registration_success_login),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Failure -> {
                    val message =
                        if (it.isNetworkError) "Please check your internet" else it.message
                            ?: it.errorBody
                    showDialog(message ?: "Please try again", true)
                }
            }
        }
        binding.signUpBtn.setOnClickListener {
            val email = binding.email.text?.trim()
            val password = binding.password.text?.trim()
            val confirmPassword = binding.confirmPassword.text?.trim()
            if (validate(email, password, confirmPassword)) {
                showLoader(true)
                viewModel.register(email.toString(), password.toString())
            }
        }
    }

    private fun showLoader(show: Boolean) {
        binding.progressCircular.visibility = if (show) View.VISIBLE else View.GONE
        binding.signUpBtn.visibility = if (show) View.GONE else View.VISIBLE
        binding.email.isEnabled = !show
        binding.password.isEnabled = !show
        binding.confirmPassword.isEnabled = !show
    }

    private fun validate(
        email: CharSequence?,
        password: CharSequence?,
        confirmPassword: CharSequence?
    ): Boolean {
        if (email.validateEmail()) {
            if (password.validatePassword()) {
                if (password?.toString() == confirmPassword?.toString()) {
                    return true
                } else binding.confirmPasswordLyt.error =
                    "Confirm password and password must be the same"
            } else binding.passwordLyt.error = "Enter a valid password with at least 3 characters"
        } else binding.emailLyt.error = "Enter a valid email"
        return false
    }

    companion object {

        private const val RC_SIGN_IN: Int = 1
    }
}
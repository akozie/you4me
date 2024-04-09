package com.you4me.you4me.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentLoginBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.AuthenticationRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainActivity
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.Utils.GOOGLE_SIGN_IN_RQ_CODE
import com.you4me.you4me.utils.validateEmail
import com.you4me.you4me.utils.validatePassword

class LoginFragment :
    BaseFragment<AuthenticationViewModel, FragmentLoginBinding, AuthenticationRepository>() {

    private lateinit var you4meSignInClient: GoogleSignInClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      //  if (!isOnBoardingDone()) findNavController().navigate(R.id.action_loginFragment_to_onboardingFragment)
        if (sharedPrefHelper.getBoolean(SharedPrefHelper.IS_LOGGED_IN)){
            startActivity(Intent(requireActivity(), MainActivity::class.java))
        }
        setupViews()
        googleSignInClient()

        binding.googleTv.setOnClickListener {
            signIn()
        }
    }

    /*create the googleSignIn client*/
    private fun googleSignInClient() {
        val serverClientId = getString(R.string.default_web_id) // get the client id
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build()

        you4meSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)
    }

    private fun signIn() {
        you4meSignInClient.signOut()
        val signInIntent = you4meSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_RQ_CODE)
    }

    /*gets the selected google account from the intent*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN_RQ_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    /**
     * handles the result of successful sign in
     * */
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            startDashboard(account)
        } catch (e: ApiException) {
            //showToast(e.localizedMessage)
        }
    }

    /**
     * open the dashboard fragment if account was selected
     * */
    private fun startDashboard(account: GoogleSignInAccount?) {
        showLoader(true)
        account?.idToken?.let { it ->
            Log.d("GOOGLE_TOKEN", it)
            viewModel.signInWithGoogle(it)
            viewModel.loginResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is Resource.Success -> {
                        viewModel.getUserDetails(it.value.userId)
                        viewModel.user.observe(viewLifecycleOwner) {user ->
                            showLoader(false)
                            when (user) {
                                is Resource.Success -> {
                                    val dialog = showDialog("Login Successful", true)
                                    viewModel.saveUser(user.value)
                                    Log.d("CHECKING", user.value.toString())
                                    sharedPrefHelper.saveString(SharedPrefHelper.USER_ID, user.value.userId)
                                    sharedPrefHelper.saveBoolean(SharedPrefHelper.IS_LOGGED_IN, true)
                                    dialog.dismiss()
                                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                                }

                                is Resource.Failure -> {

                                }
                            }
                        }
                    }

                    is Resource.Failure -> {
                        val message =
                            if (it.isNetworkError) "Please check your internet" else it.message
                                ?: it.errorBody
                        showAlertDialog(requireContext(), message ?: "Please try again", "OK"){
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    private fun isOnBoardingDone(): Boolean {
        return sharedPrefHelper.getBoolean(SharedPrefHelper.IS_ONBOARDED)
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
                    val dialog = showDialog("Login Successful", true)
                    viewModel.saveUser(it.value)
                    Log.d("CHECKING", it.value.toString())
                    sharedPrefHelper.saveString(SharedPrefHelper.USER_ID, it.value.userId)
                    sharedPrefHelper.saveBoolean(SharedPrefHelper.IS_LOGGED_IN, true)
                    binding.email.text?.clear()
                    binding.password.text?.clear()
                    dialog.dismiss()
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                }

                is Resource.Failure -> {
                    val message =
                        if (it.isNetworkError) "Please check your internet" else it.message
                    showAlertDialog(requireContext(), message ?: it.errorBody ?: "Please try again", "OK"){}
                }
            }
        }
        binding.loginBtn.setOnClickListener {
            viewModel.clearUser()
            sharedPrefHelper.saveBoolean(SharedPrefHelper.IS_LOGGED_IN, false)
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
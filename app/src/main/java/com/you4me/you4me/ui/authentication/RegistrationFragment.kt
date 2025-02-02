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
import com.google.gson.Gson
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentRegistrationBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.AuthenticationRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainActivity
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.Utils.GOOGLE_SIGN_IN_RQ_CODE
import com.you4me.you4me.utils.validateEmail
import com.you4me.you4me.utils.validatePassword
import org.json.JSONObject

class RegistrationFragment :
    BaseFragment<AuthenticationViewModel, FragmentRegistrationBinding, AuthenticationRepository>("REGISTER") {
    private lateinit var you4meSignInClient: GoogleSignInClient

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        googleSignInClient()

        mixpanel?.track("Android_Register_Viewed")

        binding.googleTv.setOnClickListener {
            signIn()
        }
    }

    override fun getViewModel() = AuthenticationViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentRegistrationBinding {
        return FragmentRegistrationBinding.inflate(inflater, container, false)
    }

    override fun getRepository() = AuthenticationRepository(dataSource.buildApi(ApiCollector::class.java))

    // create the googleSignIn client
    private fun googleSignInClient() {
        val serverClientId = getString(R.string.default_web_id) // get the client id
        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
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

    // gets the selected google account from the intent
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
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
            // showToast(e.localizedMessage)
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
            viewModel.signWithGoogleLoginResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is Resource.Success -> {
                        // viewModel.clearUser()
                        viewModel.getUserDetails(it.value.userId)
                        viewModel.user.observe(viewLifecycleOwner) { user ->
                            showLoader(false)
                            when (user) {
                                is Resource.Success -> {
                                    viewModel.clearUser()
                                    val dialog = showDialog("Registration Successful", true)
                                    viewModel.saveUser(user.value)
                                    Log.d("CHECKING", user.value.toString())
                                    val gson = Gson()
                                    val userProfileJsonString = gson.toJson(user.value)
                                    sharedPrefHelper.saveString(SharedPrefHelper.USER_PROFILE, userProfileJsonString)
                                    sharedPrefHelper.saveString(
                                        SharedPrefHelper.USER_ID,
                                        user.value.userId,
                                    )
                                    sharedPrefHelper.saveBoolean(
                                        SharedPrefHelper.IS_LOGGED_IN,
                                        true,
                                    )
                                    dialog.dismiss()
                                    startActivity(
                                        Intent(
                                            requireActivity(),
                                            MainActivity::class.java,
                                        ),
                                    )
                                    requireActivity().finish()
                                }

                                is Resource.Failure -> {
                                }
                            }
                        }
                    }

                    is Resource.Failure -> {
                        val message =
                            if (it.isNetworkError) {
                                "Please check your internet"
                            } else {
                                it.message
                                    ?: it.errorBody
                            }
                        showAlertDialog(requireContext(), message ?: "Please try again", "OK") {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    private fun setupViews() {
        binding.login.setOnClickListener { findNavController().popBackStack() }
        viewModel.registerResponse.observe(viewLifecycleOwner) { user ->
            showLoader(false)
            when (user) {
                is Resource.Success -> {
                    // viewModel.clearUser()
                    viewModel.getUserDetails(user.value.userId)
                    viewModel.user.observe(viewLifecycleOwner) {
                        when (it) {
                            is Resource.Success -> {
                                viewModel.clearUser()
                                val dialog = showDialog("Registration Successful", true)
                                viewModel.saveUser(it.value)
                                Log.d("CHECKING", it.value.toString())
                                trackUserRegisteredIn(it.value.userId, it.value.email)
                                val gson = Gson()
                                val userProfileJsonString = gson.toJson(it.value)
                                sharedPrefHelper.saveString(SharedPrefHelper.USER_PROFILE, userProfileJsonString)
                                sharedPrefHelper.saveString(
                                    SharedPrefHelper.USER_ID,
                                    it.value.userId,
                                )
                                sharedPrefHelper.saveBoolean(SharedPrefHelper.IS_LOGGED_IN, true)
                                binding.email.text?.clear()
                                binding.password.text?.clear()
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
                        if (user.isNetworkError) {
                            "Please check your internet"
                        } else {
                            user.message
                                ?: user.errorBody
                        }
                    showAlertDialog(requireContext(), message ?: "Please try again", "OK") {
                        findNavController().popBackStack()
                    }
                }
            }
        }
        binding.signUpBtn.setOnClickListener {
            val email = binding.email.text?.trim()
            val password = binding.password.text?.trim()
            val confirmPassword = binding.confirmPassword.text?.trim()
            trackRegisteredButtonClicked(email.toString())
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
        confirmPassword: CharSequence?,
    ): Boolean {
        if (email.validateEmail()) {
            if (password.validatePassword()) {
                if (password?.toString() == confirmPassword?.toString()) {
                    return true
                } else {
                    binding.confirmPasswordLyt.error =
                        "Confirm password and password must be the same"
                }
            } else {
                binding.passwordLyt.error = "Enter a valid password with at least 3 characters"
            }
        } else {
            binding.emailLyt.error = "Enter a valid email"
        }
        return false
    }

    private fun trackUserRegisteredIn(
        userId: String,
        email: String,
    ) {
        val props =
            JSONObject().apply {
                put("user_id", userId)
                put("email", email)
            }
        mixpanel?.track("Android_User_Registered", props)
    }

    private fun trackRegisteredButtonClicked(email: String) {
        val props =
            JSONObject().apply {
                put("email", email)
            }
        mixpanel?.track("Android_Registered_Button_Clicked", props)
    }

    override fun onDestroy() {
        mixpanel?.flush()
        mixpanel?.optOutTracking()
        super.onDestroy()
    }
}

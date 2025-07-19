package com.you4me.you4me.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
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
import com.you4me.you4me.databinding.FragmentLoginBinding
import com.you4me.you4me.databinding.FragmentOnboardingBinding
import com.you4me.you4me.databinding.FragmentSignWithEmailAndGoogleBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.AuthenticationRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainActivity
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.Utils
import com.you4me.you4me.utils.Utils.GOOGLE_SIGN_IN_RQ_CODE
import org.json.JSONObject


class SignWithEmailAndGoogleFragment :
    BaseFragment<AuthenticationViewModel, FragmentSignWithEmailAndGoogleBinding, AuthenticationRepository>("SIGN_IN_WITH_EMAIL_OR_GOOGLE") {


    private lateinit var you4meSignInClient: GoogleSignInClient

    override fun getViewModel() = AuthenticationViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentSignWithEmailAndGoogleBinding.inflate(inflater, container, false)

    override fun getRepository() =
        AuthenticationRepository(dataSource!!.buildApi(ApiCollector::class.java))


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (sharedPrefHelper.getBoolean(SharedPrefHelper.IS_LOGGED_IN)) {
            startActivity(Intent(requireActivity(), MainActivity::class.java))
        }

        googleSignInClient()

        binding.emailTv.setOnClickListener {
            findNavController().navigate(R.id.registrationFragment)
        }
        binding.signIn.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }
        binding.googleTv.setOnClickListener {
            mixpanel?.track("Android_GoogleSignin_Button_Clicked")
            signIn()
        }
    }

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
            Log.d("NOT_TOKEN_OKAY", "OKAY")
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
            Log.d("GOOGLE_TOKEN", "GOOGLE")
        } catch (e: ApiException) {
            Log.e("GOOGLE_SIGN_IN_ERROR", "signInResult:failed code=" + e.statusCode)
             showToast(e.localizedMessage)
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
                        Log.d("NOT_TOKEN", it.value.userId)
                        trackGoogleLoginButtonClicked(it.value.userId, it.value.email)
                        viewModel.getUserDetails(it.value.userId)
                        viewModel.user.observe(viewLifecycleOwner) { user ->
                            showLoader(false)
                            when (user) {
                                is Resource.Success -> {
                                    viewModel.clearUser()
                                    val dialog = showDialog("Login Successful", true)
                                    viewModel.saveUser(user.value)
                                    Log.d("CHECKING", user.value.toString())
                                    val gson = Gson()
                                    val userProfileJsonString = gson.toJson(user.value)
                                    sharedPrefHelper.saveString(
                                        SharedPrefHelper.USER_PROFILE,
                                        userProfileJsonString
                                    )
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

    private fun showLoader(show: Boolean) {
        binding.googleTv.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun trackGoogleLoginButtonClicked(
        userId: String,
        email: String,
    ) {
        val props =
            JSONObject().apply {
                put("user_id", userId)
                put("email", email)
            }
        mixpanel?.trackLogin(userId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mixpanel?.mixpanel?.flush()
        mixpanel?.mixpanel?.optOutTracking()
    }
}
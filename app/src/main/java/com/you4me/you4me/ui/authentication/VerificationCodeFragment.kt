package com.you4me.you4me.ui.authentication

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.chaos.view.PinView
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentVerificationCodeBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.AuthenticationRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.SharedPrefHelper.Companion.RESET_TOKEN
import com.you4me.you4me.utils.SharedPrefHelper.Companion.USER_EMAIL
import com.you4me.you4me.utils.closeSoftKeyboard
import com.you4me.you4me.utils.validateEmail
import java.util.*


class VerificationCodeFragment :
    BaseFragment<AuthenticationViewModel, FragmentVerificationCodeBinding, AuthenticationRepository>("VERIFICATION_SCREEN") {

    private lateinit var timer: TextView
    private lateinit var otpView: PinView
    private var timeSeconds = 60L
    private lateinit var email: String

    override fun getViewModel() = AuthenticationViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentVerificationCodeBinding {
        return FragmentVerificationCodeBinding.inflate(inflater, container, false)
    }

    override fun getRepository() =
        AuthenticationRepository(dataSource.buildApi(ApiCollector::class.java))


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timer = binding.timer
        otpView = binding.pinView
        email = sharedPrefHelper.getString(USER_EMAIL)
        changeEmailAddress()
        startResendTimer()


        binding.verifyBtn.setOnClickListener {
//            verifyCode()
        }

        binding.sendAgain.setOnClickListener {
            showLoader(true)
            binding.sendAgain.isVisible = false
            resendOtp()
//            binding.otpResent.visibility = View.GONE
        }
        otpView.requestFocus()
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY,
        )
        otpView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (it.length == 7) {
                        closeSoftKeyboard(requireContext(), requireActivity())
                        showLoader(true)
                        verifyCode(email, it.toString())
//                        findNavController().navigate(R.id.action_verificationCodeFragment_to_changePasswordFragment)
                    }
                    binding.verifyBtn.isEnabled = true
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun verifyCode(email: String, code: String) {
        viewModel.verifyCode(email, code)
        viewModel.verifyCodeResponse.observe(viewLifecycleOwner) {
            showLoader(false)
            when (it) {
                is Resource.Success -> {
                    sharedPrefHelper.saveString(RESET_TOKEN, it.value.reset_token)
                    showToast("Verification successful")
                    findNavController().navigate(R.id.action_verificationCodeFragment_to_changePasswordFragment)
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

    private fun resendOtp(){
        viewModel.requestPasswordReset(email)

        viewModel.registerRequestPasswordResetResponse.observe(viewLifecycleOwner) {
                showLoader(false)
                when (it) {
                    is Resource.Success -> {
                        binding.sendAgain.isVisible = false
                        binding.timer.isVisible = true
                        timeSeconds = 59L
                        startResendTimer()
                        showToast(it.value.message)
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


    private fun startResendTimer() {
        timer.isEnabled = false
        val timer = Timer()

        // Schedule a task to run repeatedly at a fixed rate
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Code to run repeatedly at a fixed rate
                timeSeconds--
                activity?.runOnUiThread {
                    this@VerificationCodeFragment.timer.text = "00:$timeSeconds"
                }
                if (timeSeconds <= 0) {
                    timeSeconds = 59L
                    timer.cancel()
                    activity?.runOnUiThread {
                        this@VerificationCodeFragment.timer.isEnabled = true
                        binding.sendAgain.isVisible = true
                        binding.timer.isVisible = false
                    }
                }
            }
        }, 0, 1000) // run 1000 milliseconds (1 second)

    }

    private fun changeEmailAddress(){
        val textView = binding.changeEmailAddress
        val clickablePart = "Change email address"
        val fullText = getString(R.string.go_ahead, clickablePart)

        val spannable = SpannableString(fullText)

        val startIndex = fullText.indexOf(clickablePart)
        val endIndex = startIndex + clickablePart.length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Navigate to another screen here
                findNavController().navigate(R.id.action_verificationCodeFragment_to_forgotPasswordFragment)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.parseColor("#ED0D57") // Match your color
                ds.isUnderlineText = true
            }
        }

        spannable.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT

    }

    private fun showLoader(show: Boolean) {
        binding.progressCircular.visibility = if (show) View.VISIBLE else View.GONE
        binding.verifyBtn.visibility = if (show) View.GONE else View.VISIBLE
        binding.sendAgain.visibility = if (show) View.GONE else View.VISIBLE
        binding.verifyBtn.isEnabled = !show
    }
}
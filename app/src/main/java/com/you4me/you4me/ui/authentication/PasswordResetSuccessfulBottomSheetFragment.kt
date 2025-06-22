package com.you4me.you4me.ui.authentication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentChangePasswordBinding
import com.you4me.you4me.databinding.FragmentPasswordResetSuccessfulBottomSheetBinding


class PasswordResetSuccessfulBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentPasswordResetSuccessfulBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPasswordResetSuccessfulBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginPageBtn.setOnClickListener {
            findNavController().navigate(R.id.action_passwordResetSuccessfulBottomSheetFragment_to_loginFragment)
        }
    }
}
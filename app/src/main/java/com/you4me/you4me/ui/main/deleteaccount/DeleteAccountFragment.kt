package com.you4me.you4me.ui.main.deleteaccount

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.Gson
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentDeleteAccountBinding
import com.you4me.you4me.databinding.FragmentProfileBinding
import com.you4me.you4me.model.User
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.ProfileRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.profile.ProfileViewModel
import com.you4me.you4me.utils.SharedPrefHelper


class DeleteAccountFragment :
    BaseFragment<ProfileViewModel, FragmentDeleteAccountBinding, ProfileRepository>("DELETE_ACCOUNT") {

    private lateinit var user: User


    override fun getViewModel() = ProfileViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentDeleteAccountBinding.inflate(inflater, container, false)

    override fun getRepository() = ProfileRepository(dataSource.buildApi(ApiCollector::class.java))


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        val gson = Gson()
        val newUser: User? = gson.fromJson(userProfile, User::class.java)
        if (newUser != null) {
            user = newUser
        }

        binding.deleteAccBtn.setOnClickListener {
            if(
                binding.checkbox.isChecked ||
                binding.checkbox1.isChecked ||
                binding.checkbox2.isChecked ||
                binding.checkbox3.isChecked ||
                binding.checkbox4.isChecked ||
                binding.checkbox5.isChecked
                    ) {
                val alertDialog = AlertDialog.Builder(ctx)
                alertDialog.setTitle("Delete account?")
                alertDialog.setMessage("Selecting delete will delete your account forever. This action is not reversible")
                alertDialog.setPositiveButton("Cancel") { dialog, int ->
                    dialog.dismiss()
                }
                alertDialog.setNegativeButton("Delete") { dialog, int ->
                    dialog.dismiss()
                    val dialogg = showDialog("Please wait", false)
                    viewModel.deleteUser(user.userId)
                    viewModel.deleteUserResponse.observe(viewLifecycleOwner) {
                        dialogg.dismiss()
                        when (it) {
                            is Resource.Success -> {
                                trackProfileDeleted()
                                showToast("Account Deleted Successfully!", Toast.LENGTH_LONG)
                                sharedPrefHelper.clearTempPreferences()
                                dialog.dismiss()
                                requireActivity().finish()
                            }

                            is Resource.Failure -> {
                                dialog.dismiss()
                                showAlertDialog(
                                    requireContext(),
                                    it.message ?: it.errorBody ?: "",
                                    "OK",
                                ) {}
                            }
                        }
                    }
                }
                alertDialog.show()
            } else {
                showToast("Please you need to select one of the options")
            }
        }

    }

    private fun trackProfileDeleted() {
        mixpanel?.track("Android_Profile_Delete_Button_Clicked")
    }

}
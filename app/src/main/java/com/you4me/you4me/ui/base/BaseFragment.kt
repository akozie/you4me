package com.you4me.you4me.ui.base

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.you4me.you4me.network.RemoteDataSource
import com.you4me.you4me.repository.BaseRepository

abstract class BaseFragment<VM : ViewModel, B : ViewBinding, R : BaseRepository> : Fragment() {

    protected val dataSource = RemoteDataSource()
    protected lateinit var binding: B
    protected lateinit var viewModel: VM
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getFragmentBinding(inflater, container)
        val factory = ViewModelFactory(getRepository())
        viewModel = ViewModelProvider(this, factory)[getViewModel()]

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })

        return binding.root
    }

    abstract fun getViewModel(): Class<VM>
    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): B
    abstract fun getRepository(): R



    fun showDialog( message : String, cancelable: Boolean = true) : Dialog {
        val builder = AlertDialog.Builder(this.requireActivity()).create()
        builder.setCancelable(cancelable)
        builder.setMessage(message)
        builder.show()
        return builder
    }
}
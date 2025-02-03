package com.you4me.you4me.ui.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.you4me.you4me.database.AppDatabase
import com.you4me.you4me.network.RemoteDataSource
import com.you4me.you4me.repository.BaseRepository
import com.you4me.you4me.repository.DbRepository
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.UtilityParam.MIXPANEL_SECRET_KEY

abstract class BaseFragment<VM : ViewModel, B : ViewBinding, R : BaseRepository>(private val screenName: String) : Fragment() {
    protected val dataSource = RemoteDataSource()
    protected lateinit var binding: B
    protected lateinit var viewModel: VM
    protected lateinit var ctx: Context
    protected lateinit var sharedPrefHelper: SharedPrefHelper
    var mixpanel: MixpanelAPI? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = getFragmentBinding(inflater, container)
        val factory = ViewModelFactory(getRepository(), getDbRepository())
        viewModel = ViewModelProvider(this, factory)[getViewModel()]
        ctx = requireContext()
        sharedPrefHelper = SharedPrefHelper(ctx)

        return binding.root
    }

    abstract fun getViewModel(): Class<VM>

    abstract fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): B

    abstract fun getRepository(): R

    private fun getDbRepository() = DbRepository(AppDatabase(requireContext()))

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mixpanel = MixpanelAPI.getInstance(context, MIXPANEL_SECRET_KEY)
    }

    override fun onResume() {
        super.onResume()
        trackScreenVisit()
    }

    private fun trackScreenVisit() {
        mixpanel?.track(screenName)
    }

    override fun onDestroy() {
        mixpanel?.flush()
        super.onDestroy()
    }

    fun showDialog(
        message: String,
        cancelable: Boolean = true,
    ): Dialog {
        val builder = AlertDialog.Builder(this.requireActivity()).create()
        builder.setCancelable(cancelable)
        builder.setMessage(message)
        builder.show()
        return builder
    }

    fun showAlertDialog(
        context: Context,
        message: String,
        positiveButtonTitle: String,
        onPositiveButtonClick: () -> Unit,
    ) {
        val alertDialog =
            android.app.AlertDialog.Builder(context).setMessage(message)
                .setPositiveButton(positiveButtonTitle) { _, _ ->
                    onPositiveButtonClick()
                    // Dismiss the dialog when "Yes" is clicked
                    // alertDialog(context).dismiss()
                }.setCancelable(false).create()

        alertDialog.show()
    }

    fun showToast(
        message: String,
        length: Int = Toast.LENGTH_SHORT,
    ) {
        Toast.makeText(requireContext(), message, length).show()
    }
}

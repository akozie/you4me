package com.you4me.you4me.ui.main

import android.R
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.you4me.you4me.databinding.FragmentGoOnDateBinding
import com.you4me.you4me.models.SubmitDateBody
import com.you4me.you4me.models.ValueLabelResponse
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GoOnDateFragment : BaseFragment<MainViewModel, FragmentGoOnDateBinding, MainRepository>() {

    private lateinit var calendar: Calendar
    private lateinit var dateFormat: SimpleDateFormat

    private lateinit var paymentModes : ArrayList<ValueLabelResponse>

    private val startAutoComplete =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    binding.searchDateLocations.setText("${place.name}, ${place.address}")
                } else {
                    Log.d("Place Result", "Intent Null")
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Log.d("Place Result", "Cancelled")
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        binding.searchDateLocations.setOnClickListener {
            startAutoCompleteIntent()
        }
        dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.UK)
        calendar = Calendar.getInstance()
        updateProposedDate()
        binding.time.text = "12:00"
        val date = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            updateProposedDate()
        }

        binding.date.setOnClickListener {
            DatePickerDialog(
                ctx,
                date,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val time = TimePickerDialog.OnTimeSetListener { timePicker, i, i2 ->
            val hour = i.toString().padStart(2, '0')
            val minute = i.toString().padStart(2, '0')
            binding.time.text = "$hour:$minute"
        }

        binding.time.setOnClickListener {
            TimePickerDialog(ctx, time, 12, 0, true).show()
        }

        binding.saveBtn.setOnClickListener {
            if (binding.searchDateLocations.text != null && binding.searchDateLocations.text.toString().length > 3) {
                viewModel.submitDate(
                    binding.date.text.toString(),
                    paymentModes[binding.whoPaysSpinner.selectedItemPosition].value,
                    binding.searchDateLocations.text.toString(),
                    binding.time.text.toString()
                )
            } else showToast("Please select a location for your date")
        }
    }

    private fun setupObservers() {
        viewModel.paymentModes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    paymentModes = it.value
                    setupSpinner(it.value)
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }
        viewModel.submitDateResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    showDialog("Date submission successful!!")
                }

                is Resource.Failure -> {
                    showDialog(it.error ?: it.message ?: it.errorBody ?: "")
                }
            }
        }
    }

    private fun setupSpinner(values: ArrayList<ValueLabelResponse>) {
        val labels = values.map { it.label }
        ArrayAdapter(
            requireContext(), R.layout.simple_spinner_item, labels
        ).also { adapter -> binding.whoPaysSpinner.adapter = adapter }
    }

    private fun startAutoCompleteIntent() {
        val fields = arrayListOf(Place.Field.NAME, Place.Field.ADDRESS)

        val intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setTypesFilter(listOf(PlaceTypes.ESTABLISHMENT)).build(ctx)
        startAutoComplete.launch(intent)
    }

    private fun updateProposedDate() {
        binding.date.text = dateFormat.format(calendar.time)
    }

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentGoOnDateBinding.inflate(layoutInflater)

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))
}
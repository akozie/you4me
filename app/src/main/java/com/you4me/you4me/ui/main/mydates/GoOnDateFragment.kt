package com.you4me.you4me.ui.main.mydates

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import com.you4me.you4me.R
import com.you4me.you4me.adapter.mydates.DateTypeAdapter
import com.you4me.you4me.databinding.FragmentGoOnDateBinding
import com.you4me.you4me.model.User
import com.you4me.you4me.models.ValueLabelResponse
import com.you4me.you4me.models.mydates.DateOption
import com.you4me.you4me.models.useroptions.PaymentModes
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainViewModel
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.Utils
import com.you4me.you4me.utils.closeSoftKeyboard
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class GoOnDateFragment :
    BaseFragment<MainViewModel, FragmentGoOnDateBinding, MainRepository>("GO_ON_DATE") {
    private lateinit var calendar: Calendar
    private lateinit var dateFormat: SimpleDateFormat
    private lateinit var user: User
    private lateinit var layouts: Map<String, LinearLayout>
    private lateinit var icons: Map<String, Int>
    private lateinit var paymentModes: ArrayList<PaymentModes>
    private var selectedOptionValue: String? = null
    private lateinit var dateType: String

    private val startAutoComplete =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    binding.searchDateLocations.text = "${place.name}, ${place.address}"
                    closeSoftKeyboard(requireContext(), requireActivity())
                } else {
//                    Log.d("Place Result", "Intent Null")
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
//                Log.d("Place Result", "Cancelled")
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
//        setupViews()
//        setupObservers()
        mixpanel?.track("Android_Request_Date_Viewed")

        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        val gson = Gson()
        user = gson.fromJson(userProfile, User::class.java)

        val splitLayout = binding.optionSplit
        val youLayout = binding.optionYou
        val theyLayout = binding.optionThey

        layouts = mapOf(
            "0" to splitLayout,
            "1" to youLayout,
            "2" to theyLayout
        )

        // Inflate icons dynamically (example placeholders used here)
        icons = mapOf(
            "0" to R.drawable.split,
            "1" to R.drawable.you_pay,
            "2" to R.drawable.they_pay
        )

        setupViews()
        setupObservers()
        dateType()
    }

    private fun setupViews() {
        binding.searchDateLocations.setOnClickListener {
            startAutoCompleteIntent()
        }
        dateFormat = Utils.getDateFormat()
        calendar = Calendar.getInstance()
        updateProposedDate()
        binding.time.text = "12:00"
        val date =
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                updateProposedDate()
            }

        binding.date.setOnClickListener {
            val sevenDaysFromNow = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 7)
            }

            val date = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                updateProposedDate()
            }

            val datePickerDialog = DatePickerDialog(
                ctx,
                date,
                sevenDaysFromNow.get(Calendar.YEAR),
                sevenDaysFromNow.get(Calendar.MONTH),
                sevenDaysFromNow.get(Calendar.DAY_OF_MONTH)
            )

            // Restrict only to dates from 7 days later and beyond
            datePickerDialog.datePicker.minDate = sevenDaysFromNow.timeInMillis

            datePickerDialog.show()
        }

        val time =
            TimePickerDialog.OnTimeSetListener { timePicker, i, i2 ->
                val hour = i.toString().padStart(2, '0')
                val minute = i2.toString().padStart(2, '0')
                binding.time.text = "$hour:$minute"
            }

        binding.time.setOnClickListener {
            TimePickerDialog(ctx, time, 12, 0, true).show()
        }

        binding.saveBtn.setOnClickListener {
            val locationText = binding.searchDateLocations.text?.toString()?.trim()
            val dateText = binding.date.text?.toString()?.trim()
            val timeText = binding.time.text?.toString()?.trim()

            when {
                dateType == null -> {
                    showToast("Please choose the date type.")
                    return@setOnClickListener
                }

                selectedOptionValue == null -> {
                    showToast("Please choose who will be paying for the date.")
                    return@setOnClickListener
                }

                locationText.isNullOrEmpty() || locationText.length < 4 -> {
                    showToast("Please enter a valid location for your date.")
                    return@setOnClickListener
                }

                else -> {
                    if (binding.customLayout.customInput.text.toString().isNotEmpty()) {
                        dateType = binding.customLayout.customInput.text.toString().trim()
                        Log.d("SELECTED_OPTION", "${dateType}")
                    }
                    viewModel.submitDate(
                        dateText.orEmpty(),
                        selectedOptionValue.toString(),
                        locationText,
                        timeText.orEmpty(),
                        user.userId,
                        dateType
                    )
                    mixpanel?.track("Android_Request_Date_Button_Pressed")
                    showLoader(true)
                }
            }
        }

    }

    private fun setupObservers() {
//        viewModel.fetchPaymentModes()
//        viewModel.paymentModes.observe(viewLifecycleOwner) {
//            when (it) {
//                is Resource.Success -> {
//                    paymentModes = it.value
////                    setupSpinner(it.value)
//                    setUpWhoIsPayingList(it.value)
//                }
//
//                is Resource.Failure -> {
//                    showAlertDialog(requireContext(), it.message ?: it.errorBody ?: "", "OK") {}
//                }
//            }
//        }
        viewModel.getUserOptions()
        viewModel.getUserOptionsResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    paymentModes = it.value.paymentModes
//                    setupSpinner(it.value)
                    setUpWhoIsPayingList(it.value.paymentModes)
                }

                is Resource.Failure -> {
                    showAlertDialog(requireContext(), it.message ?: it.errorBody ?: "", "OK") {}
                }
            }
        }
        viewModel.submitDateResponse.observe(viewLifecycleOwner) {
            showLoader(false)
            when (it) {
                is Resource.Success -> {
                    showAlertDialog(requireContext(), "Date submission successful!!", "OK") {
                        binding.date.text = "2025/01/06"
                        binding.searchDateLocations.text = ""
                        binding.time.text = "12:00"
                    }
                }

                is Resource.Failure -> {
                    try {
                        val jsonObject = JSONObject(it.errorBody)
                        val error = jsonObject.getString("error")
                        showAlertDialog(requireContext(), error ?: it.message ?: "", "OK") {}
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        showAlertDialog(requireContext(), it.message ?: "", "OK") {}
                    }
                }
            }
        }
    }

    private fun setupSpinner(values: ArrayList<ValueLabelResponse>) {
        val labels = values.map { it.label }
        ArrayAdapter(
            requireContext(),
            R.layout.spinner_item_layout,
            labels,
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

    private fun showLoader(show: Boolean) {
        binding.progressCircular.visibility = if (show) View.VISIBLE else View.GONE
        binding.saveBtn.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentGoOnDateBinding.inflate(layoutInflater)

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))


    private fun setUpWhoIsPayingList(paymentOptions: ArrayList<PaymentModes>) {
        paymentOptions.forEach { option ->
            val layout = layouts[option.value]
            layout?.removeAllViews()

            val imageView = ImageView(requireContext()).apply {
                setImageResource(icons[option.value] ?: R.drawable.ic_replay)
                layoutParams = LinearLayout.LayoutParams(64, 64)
            }

            val textView = TextView(requireContext()).apply {
                text = option.label
                gravity = Gravity.CENTER_HORIZONTAL
                textSize = 14f
                setTextColor(Color.BLACK)
            }

            layout?.apply {
                addView(imageView)
                addView(textView)
                updateLayoutStroke(this, option.value == selectedOptionValue)

                setOnClickListener {
                    selectedOptionValue = option.value
                    updateAllStrokes(paymentOptions)
                }
            }
        }
    }

    private fun updateAllStrokes(options: List<PaymentModes>) {
        options.forEach { option ->
            val layout = layouts[option.value]
            updateLayoutStroke(layout, option.value == selectedOptionValue)
        }
    }

    private fun updateLayoutStroke(layout: LinearLayout?, isSelected: Boolean) {
        layout?.background = ContextCompat.getDrawable(
            requireContext(),
            if (isSelected) R.drawable.bg_selected_stroke else R.drawable.bg_unselected_stroke
        )
    }


    private fun dateType() {

        val spinner: Spinner = binding.dateTypeSpinner
        val customInput = binding.customInput

        val options = listOf(
            DateOption(R.drawable.coffee, "Coffee", "Perfect for"),
            DateOption(R.drawable.dinner, "Dinner", "Relaxed dining"),
            DateOption(R.drawable.city_walk, "City walk", "Relaxed walking"),
            DateOption(R.drawable.movies, "Movies", "Fun watching"),
            DateOption(R.drawable.museum, "Museum", "Cultural visit"),
            DateOption(
                R.drawable.something_else,
                "Something else",
                "Custom option",
                isCustomOption = true
            )
        )

        val adapter = DateTypeAdapter(requireContext(), options)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedOption = options[position]
                if (selectedOption.isCustomOption) {
                    customInput.visibility = View.VISIBLE
                } else {
                    dateType = selectedOption.title
                    customInput.visibility = View.GONE
                    binding.customLayout.customInput.text.clear()
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                customInput.visibility = View.GONE
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mixpanel?.mixpanel?.flush()
        mixpanel?.mixpanel?.optOutTracking()
    }
}

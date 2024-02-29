package com.you4me.you4me.ui.main

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.you4me.you4me.databinding.FragmentGoOnDateBinding
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment

class GoOnDateFragment : BaseFragment<MainViewModel, FragmentGoOnDateBinding, MainRepository>() {

    private val startAutoComplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    println("Not null")
                    val place = Autocomplete.getPlaceFromIntent(intent)

//                    //fill text input
                    binding.searchDateLocations.setText("${place.name}, ${place.address}")
                } else {
                    Log.d("Place Result", "Intent Null")
                }
            }
            else if (result.resultCode == Activity.RESULT_CANCELED) {
                Log.d("Place Result", "Cancelled")
            }
        }

    private fun startAutoCompleteIntent() {
        val fields = arrayListOf(Place.Field.NAME, Place.Field.ADDRESS)

        val intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setTypesFilter(listOf(PlaceTypes.ESTABLISHMENT)).build(ctx)
        startAutoComplete.launch(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchDateLocations.setOnClickListener {
            startAutoCompleteIntent()
        }
    }

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentGoOnDateBinding.inflate(layoutInflater)

    override fun getRepository() = MainRepository()
}
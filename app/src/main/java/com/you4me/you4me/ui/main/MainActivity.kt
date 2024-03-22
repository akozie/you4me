package com.you4me.you4me.ui.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.libraries.places.api.Places
import com.you4me.you4me.BuildConfig
import com.you4me.you4me.R
import com.you4me.you4me.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()
        initializePlacesSdk()
        createNotificationChannel()
    }

    private fun setupViews() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        binding.bottomNavBar.setupWithNavController(navHostFragment.findNavController())
    }

    private fun initializePlacesSdk() {
        val placesSecretKey = BuildConfig.PLACES_SECRET_KEY
        if (placesSecretKey.isEmpty() || placesSecretKey == "DEFAULT_API_KEY") {
            Log.e("Google Places API", "No Api Key")
            return
        }
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, placesSecretKey)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "You4meNotificationChannel"
            val description = "Channel for You4me FCM notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("YOU_4_ME_CHANNEL_ID", name, importance)
            channel.description = description

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
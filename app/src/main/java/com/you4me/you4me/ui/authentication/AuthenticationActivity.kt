package com.you4me.you4me.ui.authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.you4me.you4me.databinding.ActivityAuthenticationBinding

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAuthenticationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }
}
package com.you4me.you4me.ui.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.places.api.Places
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.you4me.you4me.BuildConfig
import com.you4me.you4me.R
import com.you4me.you4me.database.AppDatabase
import com.you4me.you4me.databinding.ActivityMainBinding
import com.you4me.you4me.models.User
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.RemoteDataSource
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.DbRepository
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.authentication.AuthenticationViewModel
import com.you4me.you4me.ui.profile.ProfileViewModel
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.Utils.showAlertDialog


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
//    private val viewModel by viewModels<MainViewModel>()
    private lateinit var viewModel: MainViewModel
    private lateinit var repository: MainRepository
    private lateinit var user: User
    private lateinit var sharedPrefHelper: SharedPrefHelper
   private lateinit var firebaseInstance: FirebaseMessaging


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = MainRepository(RemoteDataSource().buildApi(ApiCollector::class.java))
        viewModel = MainViewModel(repository, DbRepository(AppDatabase.invoke(this)))
        sharedPrefHelper = SharedPrefHelper(this)
        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        val gson = Gson()
        user = gson.fromJson(userProfile, User::class.java)
       // viewModel.getNewUser(this, user.userId)
        setupViews()
        initializePlacesSdk()
        createNotificationChannel()

        firebaseInstance = FirebaseMessaging.getInstance()
        getFireBaseToken(firebaseInstance) {
            val obj = JsonObject()
            obj.addProperty("pushToken", it)
            sendTokenToBackend(obj, user.userId)
        }
    }

    private fun getFireBaseToken(
        firebaseMessagingInstance: FirebaseMessaging,
        actionToPerformWithTheReceivedToken: (received: String) -> Unit,
    ) {
        firebaseMessagingInstance.token.addOnCompleteListener(
            OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                val token = task.result
                actionToPerformWithTheReceivedToken(token)
            },
        )
    }

    private fun sendTokenToBackend(
        token: JsonObject,
        userId: String
    ) {
        viewModel.pushToken(token, userId)
    }
    private fun setupViews() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        binding.bottomNavBar.setupWithNavController(navHostFragment.findNavController())


        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.notificationsFragment -> {
                        binding.bottomNavBar.visibility = View.GONE
                    }

                    else -> binding.bottomNavBar.visibility = View.VISIBLE
                }
            }
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

//    override fun onBackPressed() {
//        super.onBackPressed()
////        viewModel.dbUser.observe(this) {
////            user = it
////        }
//        showAlertDialog(this, "Do you want to logout?", "YES", "NO", {
//            viewModel.logout(user.userId)
//            viewModel.logoutResponse.observe(this) {
//                when (it) {
//                    is Resource.Success -> {
//                        Toast.makeText(this,"Account logged out successfully!", Toast.LENGTH_SHORT).show()
//                        sharedPrefHelper.saveBoolean(SharedPrefHelper.IS_LOGGED_IN, false)
//                        //change shared pref to is logged out
//                        finish()
//                    }
//                    is Resource.Failure -> {
//                        Toast.makeText(this,it.message ?: it.errorBody ?: "", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }, {})
//    }
}
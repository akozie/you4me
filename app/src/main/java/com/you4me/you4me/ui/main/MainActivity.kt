package com.you4me.you4me.ui.main

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.places.api.Places
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.you4me.you4me.R
import com.you4me.you4me.TokenRefreshReceiver
import com.you4me.you4me.core.AppDatabase
import com.you4me.you4me.core.DbRepository
import com.you4me.you4me.databinding.ActivityMainBinding
import com.you4me.you4me.model.User
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.RemoteDataSource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.utils.LocaleHelper
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.UtilityParam

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    //    private val viewModel by viewModels<MainViewModel>()
    private lateinit var viewModel: MainViewModel
    private lateinit var repository: MainRepository
    private lateinit var user: User
    private lateinit var sharedPrefHelper: SharedPrefHelper
    private lateinit var firebaseInstance: FirebaseMessaging
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private var hasNavigatedToProfile = false  // To prevent multiple navigations

    // 1️⃣ Register the permission launcher
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("FCM", "Notification permission granted")
            } else {
                Log.e("FCM", "Notification permission denied")
                Toast.makeText(this, "Notifications are disabled", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Setup Navigation
         navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        onBackPressedDispatcher.addCallback(this) {
            val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()

            if (currentFragment is HomeFragment) {
                // Exit app if you're in the main fragment
                finishAffinity()
            } else {
                // Navigate up in the back stack
                findNavController(R.id.nav_host_fragment_container).navigateUp()
            }
        }

//        val navigateTo = intent.getStringExtra("navigate_to")
//        if (navigateTo == "profile") {
//            navController.navigate(R.id.profileFragment)
//
//            // Clear the intent extra so it doesn't persist
//            intent.removeExtra("navigate_to")
//        }

//        // Restore state to prevent multiple navigations
//        if (savedInstanceState != null) {
//            hasNavigatedToProfile = savedInstanceState.getBoolean("hasNavigatedToProfile", false)
//        }
//
//        // Navigate to ProfileFragment only once
//        if (!hasNavigatedToProfile && intent?.getStringExtra("navigate_to") == "profile") {
//            navController.navigate(R.id.profileFragment)
//            hasNavigatedToProfile = true  // Mark as navigated
//        }


//        // Handle one-time navigation
//        if (intent?.getStringExtra("navigate_to") == "profile") {
//            intent.removeExtra("navigate_to") // Clear intent extra to prevent re-triggering
//
//            // Navigate to ProfileFragment only if we're on the HomeFragment
//            if (navController.currentDestination?.id == R.id.homeFragment) {
//                navController.navigate(R.id.profileFragment)
//            }
//        }


        repository =
            MainRepository(
                RemoteDataSource().buildApi(
                    ApiCollector::class.java,
                ),
            )
        viewModel =
            MainViewModel(
                repository,
                DbRepository(
                    AppDatabase.invoke(this),
                ),
            )
        sharedPrefHelper = SharedPrefHelper(this)
        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        val gson = Gson()
        user = gson.fromJson(userProfile, User::class.java)
        // viewModel.getNewUser(this, user.userId)
        firebaseInstance = FirebaseMessaging.getInstance()
        getFireBaseToken(firebaseInstance) {
            val obj = JsonObject()
            obj.addProperty("pushToken", it)
            sendTokenToBackend(obj, user.userId)
        }

        askNotificationPermission()
        setupViews()
        initializePlacesSdk()
        createNotificationChannel()

        scheduleTokenRefresh(this)

    }



    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applySavedLocale(newBase))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("hasNavigatedToProfile", hasNavigatedToProfile)
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
                Log.d("CHECKING_VHEK", "$token")
                actionToPerformWithTheReceivedToken(token)
            },
        )
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("FCM", "Notification permission already granted")
                getFireBaseToken(firebaseInstance) {
                    val obj = JsonObject()
                    obj.addProperty("pushToken", it)
                    sendTokenToBackend(obj, user.userId)
                }
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionExplanationDialog()
            } else {
                // 3️ Request permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notification Permission Required")
            .setMessage("This app needs notification permissions to send you important updates.")
            .setPositiveButton("OK") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("No Thanks", null)
            .show()
    }

    private fun sendTokenToBackend(
        token: JsonObject,
        userId: String,
    ) {
        viewModel.pushToken(token, userId)
    }

    private fun setupViews() {
        binding.bottomNavBar.setupWithNavController(
            navHostFragment
                .findNavController(),
        )

        // Manually handle Home button clicks to reset back stack
        binding.bottomNavBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    // If already on HomeFragment, pop back stack to prevent ProfileFragment from appearing
                    if (navController.currentDestination?.id != R.id.homeFragment) {
                        navController.popBackStack(R.id.homeFragment, false)
                    }
                    true
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                    true
                }
            }
        }

        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.homeFragment -> {
                        // Check if already on HomeFragment
                        if (navController.currentDestination?.id != R.id.homeFragment) {
                            navController.popBackStack(R.id.homeFragment, false) // Clear back stack
                        }
                        binding.bottomNavBar.visibility = View.VISIBLE
                    }
                    R.id.notificationsFragment, R.id.notificationViewFragment,
                    R.id.datesFragment, R.id.editProfileFragment, R.id.allDateProposalsFragment,
                    R.id.allUpcomingDatesFragment, R.id.allCompletedDatesFragment, R.id.goOnDateFragment,
                        R.id.deleteAccountFragment, R.id.dateMatchFragment
                    -> {
                        binding.bottomNavBar.visibility = View.GONE
                    }
                    R.id.chatFragment -> {
                        binding.bottomNavBar.visibility = View.GONE
                    }

                    else -> binding.bottomNavBar.visibility = View.VISIBLE
                }
            }
    }

    private fun initializePlacesSdk() {
        val placesSecretKey = UtilityParam.GOOGLE_PLACES_SECRET_KEY
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

            val notificationManager =
                getSystemService(
                    NotificationManager::class.java,
                )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleTokenRefresh(context: Context) {

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TokenRefreshReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //  Repeat every 2 minutes
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 5 * 60 * 1000, // First trigger after 2 minutes
            5 * 60 * 1000, // Repeat every 2 minutes
            pendingIntent
        )
    }

//    override fun onBackPressed() {
//        super.onBackPressed()
// //        viewModel.dbUser.observe(this) {
// //            user = it
// //        }
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

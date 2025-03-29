package com.you4me.you4me

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.work.*
import com.cloudinary.android.MediaManager
import com.google.firebase.FirebaseApp
import com.you4me.you4me.utils.MixpanelManager
import com.you4me.you4me.utils.SharedPrefHelper.Companion.APP_TOKEN
import com.you4me.you4me.utils.SharedPrefManager
import com.you4me.you4me.worker.TokenRefreshWorker
import java.util.concurrent.TimeUnit

class You4MeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        configureCloudinary()
        SharedPrefManager.init(this)
        MixpanelManager.getInstance(this) // Ensures Mixpanel is initialized
    }

    private fun configureCloudinary() {
        val config = HashMap<String, Any>()
        config.put("cloud_name", "mmuodev")
        config.put("api_key", "242463877447421")
        config.put("api_secret", "WSmcb4hg_yho18HyXrMgz9TAr9E")
        config.put("secure", true)
        MediaManager.init(this, config)
    }


}

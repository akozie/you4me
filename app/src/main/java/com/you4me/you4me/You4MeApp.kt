package com.you4me.you4me

import android.app.Application
import android.content.Context
import com.cloudinary.android.MediaManager
import com.google.firebase.FirebaseApp
import com.you4me.you4me.utils.LocaleHelper
import com.you4me.you4me.utils.MixpanelManager
import com.you4me.you4me.utils.SharedPrefManager

class You4MeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        configureCloudinary()
        SharedPrefManager.init(this)
        MixpanelManager.getInstance(this) // Ensures Mixpanel is initialized
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.applySavedLocale(base))
    }

    private fun configureCloudinary() {
        val config = HashMap<String, Any>()
        config.put("cloud_name", "mmuodev")
//        config.put("cloud_name", "acceptance")
        config.put("api_key", "242463877447421")  // 12b3-18f6
        config.put("api_secret", "WSmcb4hg_yho18HyXrMgz9TAr9E")
        config.put("secure", true)
        MediaManager.init(this, config)
    }


}
